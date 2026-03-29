# 安装失败问题修复

## 📅 修复日期：2024-03-29

## 🚨 问题

在手机中安装 APK 时提示"安装包异常，安装失败"。

## 🔍 可能的原因

1. **签名问题** - APK 未签名或签名无效
2. **权限问题** - AndroidManifest.xml 配置不当
3. **兼容性问题** - 前台服务类型在不同 Android 版本上的兼容性
4. **native 库问题** - 缺少 `extractNativeLibs` 属性

## ✅ 已修复的问题

### 1. 添加 extractNativeLibs 属性

**修改文件：** `app/src/main/AndroidManifest.xml`

**修改内容：**
```xml
<application
    android:extractNativeLibs="true"
    android:requestLegacyExternalStorage="true"
    ...>
```

**说明：**
- `extractNativeLibs="true"` - 确保正确提取 native 库
- `requestLegacyExternalStorage="true"` - 兼容 Android 10+ 的存储权限

### 2. 修复前台服务兼容性

**修改文件：** `app/src/main/AndroidManifest.xml`

**修改前：**
```xml
<service
    android:name=".SmsListenerService"
    android:enabled="true"
    android:exported="false"
    android:foregroundServiceType="specialUse" />
```

**修改后：**
```xml
<service
    android:name=".SmsListenerService"
    android:enabled="true"
    android:exported="false" />
```

**说明：**
- 移除 `foregroundServiceType` 属性
- 该属性在 Android 14+ 有严格要求
- 移除后可以兼容更多 Android 版本

### 3. 添加必要的权限

**修改文件：** `app/src/main/AndroidManifest.xml`

**添加的权限：**
```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

**说明：**
- `FOREGROUND_SERVICE` - 前台服务权限
- `WAKE_LOCK` - 保持设备唤醒权限

### 4. 配置签名

**修改文件：** `app/build.gradle`

**添加的配置：**
```gradle
signingConfigs {
    debug {
        storeFile file('debug.keystore')
        storePassword 'android'
        keyAlias 'androiddebugkey'
        keyPassword 'android'
    }
}

buildTypes {
    debug {
        signingConfig signingConfigs.debug
        minifyEnabled false
    }
    ...
}
```

**说明：**
- 配置 Debug 版本的签名
- 使用 Android 默认的调试密钥库

## 🔧 需要完成的步骤

### 步骤 1：生成调试密钥库

**方法一：使用 Android Studio（推荐）**

Android Studio 会自动生成调试密钥库，无需手动操作。

**方法二：使用命令行**

```bash
cd app
keytool -genkey -v -keystore debug.keystore \
    -storepass android \
    -alias androiddebugkey \
    -keypass android \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -dname "CN=Android Debug,O=Android,C=US"
```

**方法三：从其他项目复制**

从任何 Android 项目复制 `debug.keystore` 文件到 `app/` 目录。

### 步骤 2：验证密钥库文件

```bash
# 检查文件是否存在
ls -la app/debug.keystore

# 如果文件不存在，需要创建
```

### 步骤 3：重新构建 APK

```bash
# 清理构建
./gradlew clean

# 构建 Debug APK
./gradlew assembleDebug

# APK 位置
# app/build/outputs/apk/debug/app-debug.apk
```

### 步骤 4：验证 APK 签名

```bash
# 使用 apksigner 验证签名
$ANDROID_HOME/build-tools/34.0.0/apksigner verify \
    app/build/outputs/apk/debug/app-debug.apk

# 应该显示：Verified
```

## 📱 安装测试

### 方法一：直接安装

```bash
# 使用 adb 安装
adb install app/build/outputs/apk/debug/app-debug.apk

# 如果已安装，使用 -r 重新安装
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 方法二：通过文件管理器

1. 将 APK 传输到手机
2. 使用文件管理器找到 APK
3. 点击安装
4. 如果提示"未知来源"，允许安装

## ❓ 常见问题

### Q: 为什么还是安装失败？

A: 检查以下几点：
1. APK 是否已签名
2. 手机是否允许安装未知来源应用
3. APK 文件是否完整
4. 手机 Android 版本是否低于 minSdk (23)

### Q: 如何查看安装失败的具体原因？

A: 使用 adb 查看日志：
```bash
adb logcat | grep -i install
```

### Q: Debug APK 可以发布吗？

A: 不可以。Debug APK 使用调试签名，不能发布到应用商店。发布需要使用 Release APK 和正式签名。

### Q: 如何生成 Release APK？

A: 需要配置正式签名密钥，然后运行：
```bash
./gradlew assembleRelease
```

## 📊 修复内容总结

| 修复项 | 状态 | 说明 |
|--------|------|------|
| 添加 extractNativeLibs | ✅ 已修复 | 确保正确提取 native 库 |
| 添加 requestLegacyExternalStorage | ✅ 已修复 | 兼容 Android 10+ |
| 修复前台服务兼容性 | ✅ 已修复 | 移除不兼容的属性 |
| 添加必要权限 | ✅ 已修复 | 添加 WAKE_LOCK 权限 |
| 配置签名 | ✅ 已修复 | 配置 Debug 签名 |
| 生成密钥库 | ⚠️ 待完成 | 需要生成 debug.keystore |

## 🎯 预期结果

修复后：
- ✅ APK 可以正常安装
- ✅ 应用可以正常启动
- ✅ 所有功能正常工作
- ✅ 兼容 Android 6.0+

## 📚 相关文档

- [Android 应用签名](https://developer.android.com/studio/publish/app-signing)
- [AndroidManifest.xml 配置](https://developer.android.com/guide/topics/manifest/manifest-intro)
- [前台服务](https://developer.android.com/guide/components/foreground-services)

## 🚀 下一步

1. **生成调试密钥库**
   - 使用 Android Studio 或命令行生成

2. **重新构建 APK**
   ```bash
   ./gradlew clean
   ./gradlew assembleDebug
   ```

3. **测试安装**
   - 使用 adb 或文件管理器安装
   - 验证应用可以正常启动

4. **提交修复**
   ```bash
   git add .
   git commit -m "fix: resolve installation failure
   - add extractNativeLibs attribute
   - fix foreground service compatibility
   - add necessary permissions
   - configure debug signing"
   git push
   ```

---

**修复状态：** ✅ 代码修复完成
**阻塞项：** 需要生成 debug.keystore
**预计结果：** APK 可以正常安装
