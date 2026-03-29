# GitHub Actions 自动编译指南

本指南将帮助您使用 GitHub Actions 自动编译 Android 项目。

## 📋 目录

- [快速开始](#快速开始)
- [工作流说明](#工作流说明)
- [配置签名密钥](#配置签名密钥)
- [触发构建](#触发构建)
- [下载 APK](#下载-apk)
- [常见问题](#常见问题)

## 🚀 快速开始

### 1. 创建 GitHub 仓库

1. 在 GitHub 上创建新仓库
2. 将项目代码推送到仓库

```bash
# 初始化 git 仓库
git init

# 添加所有文件
git add .

# 提交
git commit -m "Initial commit: Android verification code app"

# 添加远程仓库
git remote add origin https://github.com/你的用户名/verification-code-app.git

# 推送到 GitHub
git branch -M main
git push -u origin main
```

### 2. 启用 GitHub Actions

推送到 GitHub 后，GitHub Actions 会自动检测到 `.github/workflows/` 目录下的工作流文件并开始构建。

## 📝 工作流说明

项目包含两个工作流：

### 1. build-android.yml - 常规构建

**触发条件：**
- 推送到 `main`、`master` 或 `develop` 分支
- 创建 Pull Request
- 手动触发

**构建内容：**
- Debug APK（未签名）
- Release APK（未签名）

**特点：**
- 无需额外配置
- 自动缓存 Gradle 依赖
- 构建产物保留 30 天

### 2. build-release.yml - Release 构建

**触发条件：**
- 推送标签（如 `v1.0.0`）
- 手动触发

**构建内容：**
- 签名的 Release APK（如果配置了签名密钥）
- 自动创建 GitHub Release

**特点：**
- 支持签名配置
- 构建产物保留 90 天
- 自动生成 Release 页面

## 🔐 配置签名密钥（可选）

如果您需要构建签名的 Release APK，需要配置签名密钥。

### 步骤 1：生成签名密钥

在本地机器上生成签名密钥：

```bash
keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias release
```

记录以下信息：
- Keystore 密码
- Key 别名
- Key 密码

### 步骤 2：将密钥转换为 Base64

```bash
# Linux/Mac
base64 -i release-keystore.jks | pbcopy

# Windows (PowerShell)
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release-keystore.jks"))
```

### 步骤 3：在 GitHub 仓库中配置 Secrets

1. 进入 GitHub 仓库页面
2. 点击 Settings > Secrets and variables > Actions
3. 点击 New repository secret
4. 添加以下 Secrets：

| Secret 名称 | 说明 | 示例 |
|------------|------|------|
| `KEYSTORE_BASE64` | Base64 编码的 keystore 文件内容 | `...base64字符串...` |
| `KEYSTORE_PASSWORD` | Keystore 密码 | `your_keystore_password` |
| `KEY_ALIAS` | Key 别名 | `release` |
| `KEY_PASSWORD` | Key 密码 | `your_key_password` |

### 步骤 4：配置签名（可选）

在 `app/build.gradle` 中添加签名配置：

```gradle
android {
    // ... 其他配置

    // 读取签名配置
    def keystorePropertiesFile = rootProject.file("app/keystore.properties")
    def keystoreProperties = new Properties()
    if (keystorePropertiesFile.exists()) {
        keystoreProperties.load(new FileInputStream(keystorePropertiesFile))
    }

    signingConfigs {
        release {
            if (keystoreProperties['storePassword']) {
                storeFile file(keystoreProperties['storeFile'])
                storePassword keystoreProperties['storePassword']
                keyAlias keystoreProperties['keyAlias']
                keyPassword keystoreProperties['keyPassword']
            }
        }
    }

    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

## 🎯 触发构建

### 方式 1：通过推送代码

```bash
# 推送到主分支，自动触发构建
git add .
git commit -m "Update code"
git push origin main
```

### 方式 2：通过 Pull Request

创建 Pull Request 会自动触发构建。

### 方式 3：手动触发

1. 进入 GitHub 仓库
2. 点击 Actions 标签
3. 选择工作流（build-android.yml 或 build-release.yml）
4. 点击 "Run workflow" 按钮
5. 选择分支并运行

### 方式 4：通过标签触发 Release 构建

```bash
# 创建并推送标签，触发 Release 构建
git tag v1.0.0
git push origin v1.0.0
```

## 📥 下载 APK

### 方式 1：从 Actions 页面下载

1. 进入 GitHub 仓库
2. 点击 Actions 标签
3. 选择最近的工作流运行
4. 滚动到底部的 "Artifacts" 部分
5. 下载对应的 APK 文件

### 方式 2：从 Releases 页面下载（仅 Release 构建）

1. 进入 GitHub 仓库
2. 点击 Releases 标签
3. 找到对应的版本
4. 下载 APK 文件

## 🔧 高级配置

### 自定义构建参数

修改 `.github/workflows/build-android.yml` 文件：

```yaml
- name: 构建 Debug APK
  run: ./gradlew assembleDebug --stacktrace

# 可以添加更多构建任务
- name: 运行测试
  run: ./gradlew test

- name: 构建 AAB
  run: ./gradlew bundleRelease
```

### 添加环境变量

在工作流文件中添加环境变量：

```yaml
env:
  KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
  BUILD_NUMBER: ${{ github.run_number }}
```

### 分支保护规则

设置分支保护规则，确保只有通过 CI 的代码才能合并：

1. Settings > Branches
2. 添加分支保护规则
3. 启用 "Require status checks to pass before merging"
4. 选择要检查的工作流

## ❓ 常见问题

### Q1: 构建失败怎么办？

**A:** 检查以下几点：
- 查看 Actions 日志中的错误信息
- 确认 Gradle 配置是否正确
- 检查依赖项是否可用
- 确认 Java 版本配置

### Q2: 如何加快构建速度？

**A:** 工作流已配置 Gradle 依赖缓存，还可以：
- 使用 `--no-daemon` 参数
- 并行执行任务
- 使用自托管的 Runner

### Q3: 构建产物保留多久？

**A:**
- Debug 构建：30 天
- Release 构建：90 天

可以在工作流文件中调整 `retention-days` 参数。

### Q4: 如何构建多个架构的 APK？

**A:** 修改 `app/build.gradle`：

```gradle
android {
    splits {
        abi {
            enable true
            reset()
            include 'armeabi-v7a', 'arm64-v8a', 'x86', 'x86_64'
            universalApk false
        }
    }
}
```

### Q5: 如何在构建中运行测试？

**A:** 在工作流中添加测试步骤：

```yaml
- name: 运行单元测试
  run: ./gradlew test

- name: 运行仪器测试
  uses: reactivecircus/android-emulator-runner@v2
  with:
    api-level: 29
    script: ./gradlew connectedCheck
```

### Q6: 签名配置是必须的吗？

**A:** 不是必须的。如果不配置签名密钥，工作流会构建未签名的 APK。未签名的 APK 可以安装，但无法发布到应用商店。

### Q7: 如何更新 Gradle 版本？

**A:** 修改工作流文件中的 Java 版本和 Gradle 配置：

```yaml
- name: 设置 JDK 17
  uses: actions/setup-java@v4
  with:
    java-version: '17'  # 修改为需要的版本
```

### Q8: 构建超时怎么办？

**A:** 可以在工作流中设置超时时间：

```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    timeout-minutes: 60  # 设置超时时间
```

## 📊 监控构建状态

### 查看构建历史

1. 进入 GitHub 仓库
2. 点击 Actions 标签
3. 查看所有工作流运行历史

### 构建徽章

可以在 README 中添加构建状态徽章：

```markdown
![Android CI](https://github.com/你的用户名/verification-code-app/actions/workflows/build-android.yml/badge.svg)
```

### 构建通知

可以配置 GitHub Actions 通知：
- Email 通知
- Slack 集成
- Discord 集成
- 自定义 Webhook

## 🎓 最佳实践

1. **使用分支策略**：使用 `main` 分支用于生产，`develop` 分支用于开发
2. **代码审查**：通过 Pull Request 合并代码
3. **自动化测试**：在 CI 中运行测试
4. **版本标签**：使用语义化版本号（如 v1.0.0）
5. **定期清理**：定期清理旧的构建产物
6. **监控构建**：关注构建失败率，及时修复问题

## 📚 参考资源

- [GitHub Actions 官方文档](https://docs.github.com/en/actions)
- [Android Gradle 插件文档](https://developer.android.com/studio/build)
- [GitHub Secrets 文档](https://docs.github.com/en/actions/security-guides/encrypted-secrets)

## 💡 提示

- 首次构建可能需要较长时间（下载依赖）
- 后续构建会使用缓存，速度会快很多
- 可以在工作流中添加更多自动化步骤（如代码检查、测试等）
- 建议定期更新 GitHub Actions 的版本

---

**祝您使用愉快！** 如有问题，请查看 GitHub Actions 日志获取详细错误信息。
