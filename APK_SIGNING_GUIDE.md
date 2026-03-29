# APK 签名完整指南

## 📋 为什么需要签名？

Android 系统要求所有 APK 必须经过数字签名才能安装。签名用于：
- ✅ 验证 APK 的完整性和来源
- ✅ 防止 APK 被篡改
- ✅ 允许应用更新（相同签名的应用才能更新）
- ✅ 授予应用特殊权限

## 🔑 签名类型

### 1. Debug 签名
- **用途**: 开发和测试
- **特点**: 自动生成，有效期长
- **安装**: 可以直接安装
- **文件**: `debug.keystore`

### 2. Release 签名
- **用途**: 正式发布
- **特点**: 需要手动创建，更安全
- **安装**: 必须签名后才能安装
- **文件**: 自定义的 `.jks` 或 `.keystore` 文件

## 🚀 方法一：使用 apksigner（推荐）

`apksigner` 是 Google 官方推荐的签名工具，支持更现代的签名方案。

### 步骤 1: 创建签名密钥库

```bash
keytool -genkey -v -keystore release.keystore \
  -alias verification-coder-app \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storepass your-store-password \
  -keypass your-key-password \
  -dname "CN=VerificationCoder,O=YourCompany,C=CN"
```

**参数说明**:
- `-keystore`: 密钥库文件名
- `-alias`: 密钥别名（用于签名）
- `-storepass`: 密钥库密码
- `-keypass`: 密钥密码
- `-validity`: 有效期（天数）
- `-dname`: 证书信息

### 步骤 2: 签名 APK

```bash
apksigner sign --ks release.keystore \
  --ks-key-alias verification-coder-app \
  --ks-pass pass:your-store-password \
  --key-pass pass:your-key-password \
  --out app-release-signed.apk \
  app-release-unsigned.apk
```

### 步骤 3: 验证签名

```bash
apksigner verify --print-certs app-release-signed.apk
```

## 🚀 方法二：使用 jarsigner

`jarsigner` 是传统的 Java 签名工具。

### 步骤 1: 创建签名密钥库

```bash
keytool -genkey -v -keystore release.keystore \
  -alias verification-coder-app \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storepass your-store-password \
  -keypass your-key-password \
  -dname "CN=VerificationCoder,O=YourCompany,C=CN"
```

### 步骤 2: 签名 APK

```bash
jarsigner -verbose -sigalg SHA256withRSA \
  -digestalg SHA256 \
  -keystore release.keystore \
  -storepass your-store-password \
  -keypass your-key-password \
  app-release-unsigned.apk \
  verification-coder-app
```

### 步骤 3: 验证签名

```bash
jarsigner -verify -verbose -certs app-release-unsigned.apk
```

### 步骤 4: 对齐 APK（可选但推荐）

```bash
zipalign -v -p 4 app-release-unsigned.apk app-release-aligned.apk
```

## 🔐 密钥库安全注意事项

### ⚠️ 重要提示

1. **永远不要提交密钥库到 Git**
   - 将 `*.keystore`、`*.jks` 添加到 `.gitignore`
   - 使用环境变量或密钥管理服务

2. **妥善保管密码**
   - 不要在代码中硬编码密码
   - 使用安全的密码管理器

3. **备份密钥库**
   - 如果丢失密钥库，无法更新应用
   - 备份到安全的位置

4. **使用强密码**
   - 密钥库密码和密钥密码应该不同
   - 使用至少 12 位的复杂密码

## 📝 .gitignore 配置

确保密钥库不会被提交到 Git：

```gitignore
# 密钥库文件
*.keystore
*.jks
*.p12
*.pfx

# 密钥配置文件
keystore.properties
release.properties
```

## 🔧 配置 Gradle 自动签名

### 方法 1: 使用 keystore.properties 文件

#### 1. 创建 keystore.properties

```properties
storePassword=your-store-password
keyPassword=your-key-password
keyAlias=verification-coder-app
storeFile=release.keystore
```

#### 2. 修改 build.gradle

```gradle
android {
    // ... 其他配置

    // 读取 keystore.properties
    def keystorePropertiesFile = rootProject.file("keystore.properties")
    def keystoreProperties = new Properties()
    keystoreProperties.load(new FileInputStream(keystorePropertiesFile))

    signingConfigs {
        release {
            keyAlias keystoreProperties['keyAlias']
            keyPassword keystoreProperties['keyPassword']
            storeFile file(keystoreProperties['storeFile'])
            storePassword keystoreProperties['storePassword']
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

### 方法 2: 使用环境变量

```gradle
android {
    signingConfigs {
        release {
            keyAlias System.getenv('KEY_ALIAS') ?: 'default-alias'
            keyPassword System.getenv('KEY_PASSWORD') ?: 'default-password'
            storeFile file(System.getenv('STORE_FILE') ?: 'release.keystore')
            storePassword System.getenv('STORE_PASSWORD') ?: 'default-password'
        }
    }

    buildTypes {
        release {
            signingConfig signingConfigs.release
        }
    }
}
```

## 🤖 GitHub Actions 自动签名

### 配置 Secrets

在 GitHub 仓库设置中添加以下 Secrets：

1. `KEYSTORE_BASE64`: Base64 编码的密钥库文件
2. `KEYSTORE_PASSWORD`: 密钥库密码
3. `KEY_ALIAS`: 密钥别名
4. `KEY_PASSWORD`: 密钥密码

### 转换密钥库为 Base64

```bash
# Linux/Mac
base64 -i release.keystore | pbcopy

# Windows (PowerShell)
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release.keystore"))
```

### 使用 GitHub Actions 签名

```yaml
- name: 解密签名密钥
  env:
    KEYSTORE_BASE64: ${{ secrets.KEYSTORE_BASE64 }}
  run: |
    echo "$KEYSTORE_BASE64" | base64 --decode > app/release.keystore

- name: 签名 Release APK
  env:
    KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
    KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
    KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
  run: |
    apksigner sign --ks app/release.keystore \
      --ks-key-alias $KEY_ALIAS \
      --ks-pass env:KEYSTORE_PASSWORD \
      --key-pass env:KEY_PASSWORD \
      --out app/build/outputs/apk/release/app-release-signed.apk \
      app/build/outputs/apk/release/app-release-unsigned.apk
```

## 📱 安装签名后的 APK

签名完成后，APK 可以通过以下方式安装：

### 方法 1: USB 安装

```bash
adb install app-release-signed.apk
```

### 方法 2: 直接传输到手机安装

1. 将 APK 文件传输到手机
2. 在文件管理器中点击 APK
3. 允许未知来源安装
4. 完成安装

### 方法 3: 通过应用商店分发

签名后的 APK 可以发布到：
- Google Play Store
- 华为应用市场
- 小米应用商店
- 其他第三方应用市场

## 🔍 验证签名信息

### 查看签名证书信息

```bash
keytool -printcert -jarfile app-release-signed.apk
```

输出示例：
```
签名者 #1

签名:
签名算法: SHA256withRSA, 2048 位
...
所有者: CN=VerificationCoder, O=YourCompany, C=CN
发布者: CN=VerificationCoder, O=YourCompany, C=CN
序列号: 1234567890abcdef
有效期开始日期: 2025-01-01
有效期截止日期: 2052-01-01
证书指纹:
         MD5:  AA:BB:CC:DD:EE:FF:11:22:33:44:55:66:77:88:99:00
         SHA1:  11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE:FF:00:11:22:33:44
         SHA256: AA:BB:CC:DD:EE:FF:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE:FF:11:22:33:44:55:66:77:88:99:AA:BB:CC
签名算法名称: SHA256withRSA
主体公共密钥算法: 2048 位 RSA 密钥
版本: 3
```

## ⚠️ 常见问题

### 1. 签名验证失败

**原因**: 密钥库密码或密钥密码错误

**解决方案**: 检查密码是否正确，确保大小写一致

### 2. 安装失败：签名冲突

**原因**: 设备上已安装相同包名但不同签名的应用

**解决方案**: 先卸载旧版本，再安装新版本

### 3. 签名后无法更新

**原因**: 使用了不同的密钥库签名

**解决方案**: 始终使用相同的密钥库进行签名

### 4. 密钥库文件损坏

**原因**: 密钥库文件被意外修改或损坏

**解决方案**: 从备份恢复密钥库文件

## 📚 参考资料

- [Android 官方签名文档](https://developer.android.com/studio/publish/app-signing)
- [apksigner 使用指南](https://developer.android.com/studio/command-line/apksigner)
- [keytool 使用指南](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/keytool.html)

## 🎯 快速参考

### 创建密钥库
```bash
keytool -genkey -v -keystore release.keystore -alias your-alias -keyalg RSA -keysize 2048 -validity 10000
```

### 使用 apksigner 签名
```bash
apksigner sign --ks release.keystore --ks-key-alias your-alias --out signed.apk unsigned.apk
```

### 使用 jarsigner 签名
```bash
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA256 -keystore release.keystore unsigned.apk your-alias
```

### 验证签名
```bash
apksigner verify --print-certs signed.apk
```

### 对齐 APK
```bash
zipalign -v -p 4 input.apk output.apk
```

---

**重要提示**: 请妥善保管你的密钥库文件和密码！如果丢失，将无法更新已发布的应用。
