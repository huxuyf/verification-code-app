# 快速部署到 GitHub

本指南将帮助您在 5 分钟内将项目部署到 GitHub 并开始自动编译。

## 🚀 快速开始（3 步）

### 第 1 步：创建 GitHub 仓库

1. 访问 [GitHub](https://github.com) 并登录
2. 点击右上角的 "+" 按钮，选择 "New repository"
3. 填写仓库信息：
   - Repository name: `verification-code-app`
   - Description: `Android verification code SMS helper`
   - 选择 Public 或 Private
   - **不要**勾选 "Initialize this repository with a README"
4. 点击 "Create repository"

### 第 2 步：运行部署脚本

**Windows 用户：**
```bash
deploy-to-github.bat
```

**Linux/Mac 用户：**
```bash
chmod +x deploy-to-github.sh
./deploy-to-github.sh
```

脚本会提示您：
1. 输入 GitHub 仓库 URL（从第 1 步复制）
2. 输入提交信息（可以留空使用默认）
3. 确认推送到 GitHub

### 第 3 步：等待构建完成

1. 推送完成后，访问您的 GitHub 仓库
2. 点击 "Actions" 标签
3. 等待构建完成（通常需要 3-5 分钟）
4. 构建成功后，点击工作流运行记录
5. 滚动到底部，在 "Artifacts" 部分下载 APK

## 📱 安装 APK

1. 下载 APK 文件到您的 Android 设备
2. 在设备上打开文件管理器，找到 APK 文件
3. 点击安装（可能需要允许"未知来源"应用）
4. 授予应用权限
5. 开始使用！

## 🔧 手动部署（不使用脚本）

如果您不想使用部署脚本，可以手动执行以下步骤：

### 1. 初始化 Git 仓库

```bash
git init
```

### 2. 添加远程仓库

```bash
git remote add origin https://github.com/huxuyf/verification-code-app.git
```

### 3. 提交代码

```bash
git add .
git commit -m "feat: initial commit - Android verification code app"
```

### 4. 推送到 GitHub

```bash
git branch -M main
git push -u origin main
```

## 📊 查看构建状态

### 方式 1：GitHub Actions 页面

1. 访问 GitHub 仓库
2. 点击 "Actions" 标签
3. 查看所有工作流运行记录

### 方式 2：构建徽章

在 README.md 中添加构建状态徽章：

```markdown
![Android CI](https://github.com/huxuyf/verification-code-app/actions/workflows/build-android.yml/badge.svg)
```

## 🎯 手动触发构建

如果您不想推送代码也能触发构建：

1. 访问 GitHub 仓库
2. 点击 "Actions" 标签
3. 左侧选择 "Android CI" 工作流
4. 点击右上角的 "Run workflow" 按钮
5. 选择分支并点击 "Run workflow"

## 📦 创建 Release 版本

### 方式 1：通过标签

```bash
# 创建标签
git tag v1.0.0

# 推送标签
git push origin v1.0.0
```

推送标签后会自动触发 Release 构建，并创建 GitHub Release 页面。

### 方式 2：通过 GitHub 界面

1. 访问 GitHub 仓库
2. 点击 "Releases" 标签
3. 点击 "Create a new release"
4. 填写版本号和说明
5. 发布后会自动构建

## 🔐 配置签名密钥（可选）

如果您需要构建签名的 Release APK，请查看 [GITHUB_ACTIONS_GUIDE.md](GITHUB_ACTIONS_GUIDE.md#配置签名密钥可选) 的详细说明。

## ❓ 常见问题

### Q: 构建失败怎么办？

A: 查看 Actions 日志中的错误信息，常见原因：
- Gradle 配置错误
- 依赖项不可用
- 网络问题

### Q: 如何加快构建速度？

A: 工作流已配置 Gradle 依赖缓存，首次构建较慢，后续构建会快很多。

### Q: 构建产物保留多久？

A:
- Debug 构建：30 天
- Release 构建：90 天

### Q: 如何删除旧的构建产物？

A: 在 Actions 页面的工作流运行记录中，可以删除旧的运行记录。

### Q: 可以同时构建多个版本吗？

A: 可以，GitHub Actions 支持并发构建多个任务。

## 📚 更多资源

- [GitHub Actions 完整指南](GITHUB_ACTIONS_GUIDE.md)
- [项目 README](README.md)
- [快速开始指南](QUICK_START.md)

## 💡 提示

- 首次构建可能需要 5-10 分钟（下载依赖）
- 后续构建通常需要 2-3 分钟
- 可以在工作流文件中自定义构建参数
- 建议使用语义化版本号（如 v1.0.0）

## ✅ 检查清单

部署前检查：

- [ ] 已创建 GitHub 仓库
- [ ] 已安装 Git
- [ ] 已配置 Git 用户信息
- [ ] 网络连接正常
- [ ] 代码已提交

部署后检查：

- [ ] 代码已推送到 GitHub
- [ ] Actions 工作流已触发
- [ ] 构建成功完成
- [ ] 已下载 APK 文件
- [ ] APK 可以正常安装

---

**祝您部署顺利！** 如有问题，请查看 [GITHUB_ACTIONS_GUIDE.md](GITHUB_ACTIONS_GUIDE.md) 或 GitHub Actions 日志。
