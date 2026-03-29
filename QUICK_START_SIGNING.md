# APK 签名快速开始指南

## 🎯 最简单的签名方法

### 方法 1: 使用自动化脚本（推荐）

#### Windows 用户：

1. **双击运行** `sign-apk.bat`
2. **按提示输入**：
   - 密钥库密码
   - 密钥密码
3. **等待完成** - 签名后的 APK 会自动生成

#### Linux/Mac 用户：

1. **赋予执行权限**：
   ```bash
   chmod +x sign-apk.sh
   ```

2. **运行脚本**：
   ```bash
   ./sign-apk.sh
   ```

3. **按提示输入**：
   - 密钥库密码
   - 密钥密码
4. **等待完成** - 签名后的 APK 会自动生成

---

### 方法 2: 使用 apksigner（命令行）

#### 1. 创建密钥库（首次使用）

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

---

## 📱 GitHub Actions 自动签名

### 配置步骤：

1. **进入 GitHub 仓库设置**
   - 点击仓库的 "Settings" 标签
   - 选择 "Secrets and variables" → "Actions"

2. **添加以下 Secrets**：

| Secret 名称 | 说明 | 示例 |
|------------|------|------|
| `KEYSTORE_BASE64` | Base64 编码的密钥库文件 | 见下方转换方法 |
| `KEYSTORE_PASSWORD` | 密钥库密码 | your-store-password |
| `KEY_ALIAS` | 密钥别名 | verification-coder-app |
| `KEY_PASSWORD` | 密钥密码 | your-key-password |

3. **转换密钥库为 Base64**：

**Linux/Mac:**
```bash
base64 -i release.keystore | pbcopy
```

**Windows (PowerShell):**
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release.keystore"))
```

4. **触发构建**：
   - 推送代码或创建标签
   - GitHub Actions 会自动签名 APK

---

## 🔍 验证签名是否成功

### 方法 1: 使用 apksigner

```bash
apksigner verify --print-certs app-release-signed.apk
```

### 方法 2: 使用 keytool

```bash
keytool -printcert -jarfile app-release-signed.apk
```

### 方法 3: 尝试安装

```bash
adb install app-release-signed.apk
```

如果成功安装，说明签名有效。

---

## ⚠️ 常见问题

### Q1: 签名后仍然无法安装？

**原因**: 可能是签名冲突（已安装不同签名的相同包名应用）

**解决**:
```bash
adb uninstall com.verificationcoder.app
adb install app-release-signed.apk
```

### Q2: 忘记密钥库密码怎么办？

**解决**: 无法找回，需要重新创建密钥库并重新签名

### Q3: 如何查看 APK 是否已签名？

**命令**:
```bash
apksigner verify --print-certs your-apk.apk
```

如果显示证书信息，说明已签名。

### Q4: 可以使用 debug.keystore 签名 Release APK 吗？

**可以，但不推荐**：
- Debug 密钥库安全性较低
- 不符合应用商店发布要求
- 仅用于个人测试

---

## 📝 文件位置

### 输入文件：
- **未签名的 APK**: `app/build/outputs/apk/release/app-release-unsigned.apk`

### 输出文件：
- **已签名的 APK**: `app/build/outputs/apk/release/app-release-signed.apk`

### 密钥库文件：
- **位置**: `release.keystore`（项目根目录）
- **⚠️ 重要**: 不要提交到 Git！

---

## 🚀 下一步

签名完成后：

1. **测试安装**：
   ```bash
   adb install app-release-signed.apk
   ```

2. **发布到应用商店**：
   - Google Play Store
   - 华为应用市场
   - 小米应用商店
   - 其他第三方市场

3. **分享给用户**：
   - 通过邮件发送
   - 上传到网盘
   - 创建下载链接

---

## 📚 详细文档

如需更详细的信息，请参考：
- [APK_SIGNING_GUIDE.md](./APK_SIGNING_GUIDE.md) - 完整签名指南
- [GITHUB_ACTIONS_BUILD_GUIDE.md](./GITHUB_ACTIONS_BUILD_GUIDE.md) - GitHub Actions 构建指南

---

## 💡 提示

- ✅ 始终使用相同的密钥库签名同一应用
- ✅ 妥善保管密钥库文件和密码
- ✅ 定期备份密钥库文件
- ❌ 不要将密钥库提交到 Git
- ❌ 不要使用弱密码

---

**快速参考命令**：

```bash
# 创建密钥库
keytool -genkey -v -keystore release.keystore -alias my-app -keyalg RSA -keysize 2048 -validity 10000

# 签名 APK
apksigner sign --ks release.keystore --ks-key-alias my-app --out signed.apk unsigned.apk

# 验证签名
apksigner verify --print-certs signed.apk

# 安装 APK
adb install signed.apk
```
