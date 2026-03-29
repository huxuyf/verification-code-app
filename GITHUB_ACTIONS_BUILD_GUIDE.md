# GitHub Actions APK 构建指南

## 📋 概述

本文档说明了如何使用 GitHub Actions 构建可安装的 APK 文件，以及解决安装失败问题的修复方案。

## 🔧 已修复的问题

### 1. AndroidManifest.xml 配置问题

#### 修复内容：
- ✅ 更新 `tools:targetApi` 从 31 到 34
- ✅ 添加 `FOREGROUND_SERVICE_DATA_SYNC` 权限
- ✅ 修正前台服务类型声明

#### 影响：
- 解决了 Android 13+ 设备上的兼容性问题
- 确保前台服务在 Android 14+ 上正常运行

### 2. build.gradle 配置优化

#### 修复内容：
- ✅ 添加 NDK ABI 过滤器配置
- ✅ 启用传统打包模式 `useLegacyPackaging`
- ✅ 增强 debug 构建配置

#### 影响：
- 确保 APK 在不同架构设备上兼容
- 解决原生库打包问题
- 提高安装成功率

### 3. GitHub Actions 工作流优化

#### 修复内容：
- ✅ 自动生成 debug.keystore（如果不存在）
- ✅ 添加构建前清理步骤
- ✅ 添加 APK 验证步骤
- ✅ 改进构建报告格式

#### 影响：
- 确保每次构建都有有效的签名密钥
- 避免缓存导致的构建问题
- 提供更清晰的构建反馈

## 🚀 使用方法

### 方法 1: 自动构建（推荐）

1. **触发构建**：
   - 推送代码到 `main`、`master` 或 `develop` 分支
   - 或创建 Pull Request
   - 或手动触发 workflow（`workflow_dispatch`）

2. **下载 APK**：
   - 进入 GitHub 仓库的 "Actions" 页面
   - 选择最近的构建任务
   - 在 "Artifacts" 部分下载 `debug-apk`

3. **安装 APK**：
   - 将 `app-debug.apk` 传输到 Android 设备
   - 在设备上安装（允许未知来源）

### 方法 2: 手动触发

1. 进入 GitHub 仓库的 "Actions" 页面
2. 选择 "Android CI" workflow
3. 点击 "Run workflow" 按钮
4. 选择分支并运行

## 📱 APK 文件说明

### Debug APK（推荐用于测试）

- **文件名**: `app-debug.apk`
- **签名**: 使用 debug.keystore 自动签名
- **安装**: 可以直接安装
- **用途**: 开发和测试

### Release APK

- **文件名**: `app-release-unsigned.apk` 或 `verification-code-app-v{version}.apk`
- **签名**: 未签名
- **安装**: 需要先签名
- **用途**: 正式发布

## 🔑 签名 Release APK

如果需要签名 Release APK，请按以下步骤操作：

### 1. 创建签名密钥

```bash
keytool -genkey -v -keystore release.keystore \
  -alias your-alias -keyalg RSA -keysize 2048 -validity 10000
```

### 2. 签名 APK

```bash
# 使用 jarsigner
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA256 \
  -keystore release.keystore app-release-unsigned.apk your-alias

# 或使用 apksigner（推荐）
apksigner sign --ks release.keystore \
  --ks-key-alias your-alias app-release-unsigned.apk
```

### 3. 验证签名

```bash
jarsigner -verify -verbose -certs app-release-unsigned.apk
```

## 📋 系统要求

- **最低 Android 版本**: Android 13 (API 33)
- **目标 Android 版本**: Android 14 (API 34)
- **必需权限**:
  - 短信接收和读取权限
  - 悬浮窗权限
  - 通知权限（Android 13+）

## ⚠️ 常见问题

### 1. 安装失败：解析包错误

**原因**:
- APK 损坏或不完整
- 签名问题
- Android 版本不兼容

**解决方案**:
- 重新下载 APK
- 确保使用正确的签名
- 检查设备 Android 版本（需要 Android 13+）

### 2. 安装失败：签名冲突

**原因**:
- 已安装相同包名但不同签名的应用

**解决方案**:
- 先卸载旧版本
- 再安装新版本

### 3. 权限被拒绝

**原因**:
- 用户未授予必要权限

**解决方案**:
- 在应用设置中手动授予权限
- 或卸载后重新安装并授予权限

## 📊 构建产物

GitHub Actions 会生成以下构建产物：

| 产物名称 | 文件路径 | 说明 |
|---------|---------|------|
| debug-apk | `app/build/outputs/apk/debug/app-debug.apk` | Debug 版本，已签名 |
| release-apk | `app/build/outputs/apk/release/app-release-unsigned.apk` | Release 版本，未签名 |

## 🔍 验证 APK

### 使用 aapt 查看信息

```bash
aapt dump badging app-debug.apk
```

### 使用 keytool 验证签名

```bash
keytool -printcert -jarfile app-debug.apk
```

## 📝 更新日志

### 2025-01-XX
- ✅ 修复 AndroidManifest.xml targetApi 版本
- ✅ 添加前台服务类型权限
- ✅ 优化 build.gradle 配置
- ✅ 改进 GitHub Actions 工作流
- ✅ 添加自动密钥生成
- ✅ 增强 APK 验证步骤

## 🆘 获取帮助

如果遇到问题，请：

1. 查看 GitHub Actions 构建日志
2. 检查设备 Android 版本
3. 确认 APK 文件完整性
4. 提交 Issue 并提供详细信息

## 📄 许可证

此项目遵循其原始许可证。
