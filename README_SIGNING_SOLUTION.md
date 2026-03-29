# APK 签名解决方案总结

## 🎯 问题说明

**问题**: Release APK 无法正常安装

**原因**: Release APK 未经过数字签名

**解决方案**: 为 APK 添加数字签名

---

## ✅ 已完成的工作

### 1. 创建详细的签名指南文档

📄 **APK_SIGNING_GUIDE.md**
- 完整的签名流程说明
- 两种签名方法（apksigner 和 jarsigner）
- 密钥库安全注意事项
- GitHub Actions 自动签名配置
- 常见问题解决方案

### 2. 创建自动化签名脚本

📄 **sign-apk.sh** (Linux/Mac)
- 自动检测密钥库
- 交互式创建密钥库
- 自动签名 APK
- 验证签名结果

📄 **sign-apk.bat** (Windows)
- Windows 批处理脚本
- 与 Linux 版本功能相同
- 支持 Windows 命令行

### 3. 更新 GitHub Actions 工作流

📄 **.github/workflows/build-release.yml**
- 支持自动签名（如果配置了 Secrets）
- 智能检测签名配置
- 生成已签名和未签名两个版本
- 改进的构建报告

### 4. 创建快速开始指南

📄 **QUICK_START_SIGNING.md**
- 最简单的签名方法
- 快速参考命令
- GitHub Actions 配置步骤
- 常见问题 FAQ

---

## 🚀 使用方法

### 方法 1: 使用自动化脚本（最简单）

#### Windows:
```bash
# 双击运行
sign-apk.bat
```

#### Linux/Mac:
```bash
# 赋予执行权限
chmod +x sign-apk.sh

# 运行脚本
./sign-apk.sh
```

### 方法 2: 手动签名

#### 1. 创建密钥库
```bash
keytool -genkey -v -keystore release.keystore \
  -alias verification-coder-app \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

#### 2. 签名 APK
```bash
apksigner sign \
  --ks release.keystore \
  --ks-key-alias verification-coder-app \
  --out app-release-signed.apk \
  app-release-unsigned.apk
```

#### 3. 验证签名
```bash
apksigner verify --print-certs app-release-signed.apk
```

### 方法 3: GitHub Actions 自动签名

#### 配置 GitHub Secrets:

1. 进入仓库 Settings → Secrets and variables → Actions
2. 添加以下 Secrets:

| Secret | 说明 |
|--------|------|
| `KEYSTORE_BASE64` | Base64 编码的密钥库 |
| `KEYSTORE_PASSWORD` | 密钥库密码 |
| `KEY_ALIAS` | 密钥别名 |
| `KEY_PASSWORD` | 密钥密码 |

#### 转换密钥库为 Base64:

**Linux/Mac:**
```bash
base64 -i release.keystore | pbcopy
```

**Windows (PowerShell):**
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release.keystore"))
```

#### 触发构建:
推送代码或创建标签，GitHub Actions 会自动签名 APK。

---

## 📋 文件清单

### 新增文件：

1. **APK_SIGNING_GUIDE.md** - 完整签名指南（详细）
2. **QUICK_START_SIGNING.md** - 快速开始指南（简洁）
3. **README_SIGNING_SOLUTION.md** - 本文档（总结）
4. **sign-apk.sh** - Linux/Mac 自动化脚本
5. **sign-apk.bat** - Windows 自动化脚本

### 修改文件：

1. **.github/workflows/build-release.yml** - 添加自动签名功能

---

## 🔍 验证签名是否成功

### 方法 1: 命令行验证
```bash
apksigner verify --print-certs app-release-signed.apk
```

### 方法 2: 尝试安装
```bash
adb install app-release-signed.apk
```

如果成功安装，说明签名有效。

### 方法 3: 查看证书信息
```bash
keytool -printcert -jarfile app-release-signed.apk
```

---

## ⚠️ 重要提示

### 安全注意事项：

1. **不要提交密钥库到 Git**
   - 将 `*.keystore`、`*.jks` 添加到 `.gitignore`
   - 使用 GitHub Secrets 管理密钥

2. **妥善保管密码**
   - 不要在代码中硬编码密码
   - 使用安全的密码管理器

3. **备份密钥库**
   - 如果丢失密钥库，无法更新应用
   - 备份到安全的位置

4. **使用强密码**
   - 密钥库密码和密钥密码应该不同
   - 使用至少 12 位的复杂密码

### 签名规则：

1. **始终使用相同的密钥库**
   - 同一应用的所有版本必须使用相同密钥库签名
   - 否则无法更新应用

2. **Debug vs Release**
   - Debug 签名: 用于开发和测试
   - Release 签名: 用于正式发布

3. **有效期**
   - 密钥库有效期建议设置为 10000 天（约 27 年）
   - 避免频繁更换密钥库

---

## 📱 安装签名后的 APK

### 方法 1: USB 安装
```bash
adb install app-release-signed.apk
```

### 方法 2: 直接传输到手机
1. 将 APK 文件传输到手机
2. 在文件管理器中点击 APK
3. 允许未知来源安装
4. 完成安装

### 方法 3: 通过应用商店发布
签名后的 APK 可以发布到：
- Google Play Store
- 华为应用市场
- 小米应用商店
- 其他第三方应用市场

---

## 🎯 预期结果

签名完成后，你应该能够：

✅ 成功安装 Release APK
✅ 在 Android 13+ 设备上正常运行
✅ 通过应用商店的安全检查
✅ 更新应用（使用相同密钥库）

---

## 🆘 遇到问题？

### 常见问题：

1. **签名验证失败**
   - 检查密码是否正确
   - 确认密钥库文件未损坏

2. **安装失败：签名冲突**
   - 先卸载旧版本
   - 再安装新版本

3. **密钥库文件损坏**
   - 从备份恢复
   - 或重新创建密钥库（无法更新旧版本）

4. **忘记密码**
   - 无法找回
   - 需要重新创建密钥库

### 获取帮助：

- 查看 [APK_SIGNING_GUIDE.md](./APK_SIGNING_GUIDE.md) 获取详细信息
- 查看 [QUICK_START_SIGNING.md](./QUICK_START_SIGNING.md) 获取快速参考
- 提交 GitHub Issue 寻求帮助

---

## 📚 参考资料

- [Android 官方签名文档](https://developer.android.com/studio/publish/app-signing)
- [apksigner 使用指南](https://developer.android.com/studio/command-line/apksigner)
- [keytool 使用指南](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/keytool.html)

---

## 🎉 总结

现在你有三种方式为 Release APK 签名：

1. **自动化脚本** - 最简单，推荐新手使用
2. **手动签名** - 更灵活，适合高级用户
3. **GitHub Actions** - 自动化，适合 CI/CD 流程

选择最适合你的方式，开始签名你的 APK 吧！

---

**最后更新**: 2025-01-XX
**版本**: 1.0
