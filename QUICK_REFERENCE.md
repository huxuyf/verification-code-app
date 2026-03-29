# GitHub Actions 快速参考

## 🚀 一键部署

### Windows
```bash
deploy-to-github.bat
```

### Linux/Mac
```bash
chmod +x deploy-to-github.sh
./deploy-to-github.sh
```

## 📝 手动部署命令

```bash
# 1. 初始化 Git
git init

# 2. 添加远程仓库
git remote add origin https://github.com/huxuyf/verification-code-app.git

# 3. 提交代码
git add .
git commit -m "feat: initial commit"

# 4. 推送到 GitHub
git branch -M main
git push -u origin main
```

## 🎯 触发构建

### 自动触发
- 推送到 `main`、`master` 或 `develop` 分支
- 创建 Pull Request
- 推送标签（如 `v1.0.0`）

### 手动触发
1. 访问 GitHub 仓库
2. 点击 "Actions" 标签
3. 选择工作流
4. 点击 "Run workflow"

## 📥 下载 APK

### 从 Actions 页面
1. 访问 GitHub 仓库 > Actions
2. 选择工作流运行记录
3. 下载 Artifacts

### 从 Releases 页面
1. 访问 GitHub 仓库 > Releases
2. 选择版本
3. 下载 APK

## 🔐 配置签名密钥（可选）

### 1. 生成密钥
```bash
keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias release
```

### 2. 转换为 Base64
```bash
# Linux/Mac
base64 -i release-keystore.jks | pbcopy

# Windows (PowerShell)
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release-keystore.jks"))
```

### 3. 配置 GitHub Secrets
进入 Settings > Secrets and variables > Actions，添加：
- `KEYSTORE_BASE64`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

## 📊 工作流说明

### build-android.yml
- **触发**：推送代码、Pull Request、手动
- **构建**：Debug APK、Release APK（未签名）
- **保留**：30 天

### build-release.yml
- **触发**：推送标签、手动
- **构建**：签名的 Release APK
- **保留**：90 天
- **创建**：GitHub Release

## 📚 文档索引

| 文档 | 用途 |
|------|------|
| [GITHUB_ACTIONS_GUIDE.md](GITHUB_ACTIONS_GUIDE.md) | 完整使用指南 |
| [QUICK_DEPLOY.md](QUICK_DEPLOY.md) | 快速部署（3 步） |
| [GITHUB_ACTIONS_SUMMARY.md](GITHUB_ACTIONS_SUMMARY.md) | 配置总结 |
| [README.md](README.md) | 项目说明 |
| [QUICK_START.md](QUICK_START.md) | 应用使用指南 |

## 🔗 快速链接

- [GitHub Actions 官方文档](https://docs.github.com/en/actions)
- [Android Gradle 插件文档](https://developer.android.com/studio/build)
- [项目仓库](https://github.com/huxuyf/verification-code-app)

## 💡 提示

- 首次构建需要 5-10 分钟
- 后续构建需要 2-3 分钟
- 使用 Gradle 缓存加速构建
- 定期清理旧的构建产物

## ❓ 常见命令

### 查看构建状态
```bash
gh run list
```

### 查看最新构建
```bash
gh run view
```

### 重新运行失败的构建
```bash
gh run rerun
```

### 取消运行中的构建
```bash
gh run cancel
```

## ✅ 检查清单

部署前：
- [ ] 已创建 GitHub 仓库
- [ ] 已安装 Git
- [ ] 网络连接正常

部署后：
- [ ] 代码已推送
- [ ] Actions 已触发
- [ ] 构建成功
- [ ] 已下载 APK

---

**快速参考** | **GitHub Actions** | **Android CI**
