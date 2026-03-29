# 安卓验证码短信弹窗助手

一款简洁实用的安卓应用，自动监听短信中的验证码并以悬浮窗形式显示，方便一键复制。

## 功能特点

- **自动监听短信**：实时监听手机接收的短信
- **智能识别验证码**：自动提取短信中4-8位数字验证码
- **关键词匹配**：识别包含"密码"或"验证码"的短信
- **悬浮窗显示**：美观的悬浮窗界面，可拖动位置
- **一键复制**：点击复制按钮即可复制验证码到剪贴板
- **隐私保护**：仅在本地处理短信数据，不上传任何信息

## 系统要求

- Android 6.0 (API 23) 及以上版本
- 支持安卓 10+ 的权限管理

## 权限说明

应用需要以下权限才能正常工作：

| 权限 | 用途 | 是否必需 |
|------|------|----------|
| RECEIVE_SMS | 接收短信广播 | 是 |
| READ_SMS | 读取短信内容 | 是 |
| SYSTEM_ALERT_WINDOW | 显示悬浮窗 | 是 |
| FOREGROUND_SERVICE | 保持监听服务存活 | 否 |

## 使用说明

### 1. 安装应用

将 APK 文件安装到您的 Android 设备上。

### 2. 授予权限

首次启动应用时，需要授予以下权限：

#### 步骤一：授予短信权限
1. 打开应用
2. 点击"授予权限"按钮
3. 在弹出的权限请求对话框中，选择"允许"

#### 步骤二：授予悬浮窗权限
1. 授予短信权限后，应用会自动请求悬浮窗权限
2. 在系统设置页面，找到"验证码助手"应用
3. 开启"在其他应用上层显示"权限

### 3. 开始使用

1. 权限授予完成后，点击"开始监听"按钮
2. 应用会在后台运行，监听短信
3. 当收到包含"验证码"或"密码"关键词的短信时，会自动弹出悬浮窗
4. 点击"复制验证码"按钮即可复制验证码
5. 点击右上角的"✕"按钮关闭悬浮窗

### 4. 停止监听

如需停止监听，点击"停止监听"按钮即可。

## 验证码提取规则

- 提取短信中连续的4-8位数字
- 如果短信中有多组数字，默认取最后出现的一组
- 只有包含"验证码"或"密码"关键词的短信才会触发弹窗

## 项目结构

```
verification_code_app/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/verificationcoder/app/
│   │       │   ├── MainActivity.java              # 主界面Activity
│   │       │   ├── SmsReceiver.java               # 短信接收器
│   │       │   ├── SmsListenerService.java        # 短信监听服务
│   │       │   ├── FloatingWindowManager.java     # 悬浮窗管理器
│   │       │   └── VerificationCodeExtractor.java # 验证码提取工具
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   │   ├── activity_main.xml          # 主界面布局
│   │       │   │   └── floating_window.xml        # 悬浮窗布局
│   │       │   ├── drawable/                      # 图形资源
│   │       │   ├── values/                        # 值资源
│   │       │   └── mipmap-*/                      # 应用图标
│   │       └── AndroidManifest.xml                 # 应用清单文件
│   └── build.gradle                               # 应用级构建配置
├── build.gradle                                   # 项目级构建配置
└── settings.gradle                                # Gradle设置文件
```

## 编译说明

### 方式一：使用 GitHub Actions 自动编译（推荐）

项目已配置 GitHub Actions，可以自动编译 APK 文件。

#### 快速开始

1. **将代码推送到 GitHub**

   使用提供的部署脚本：

   **Windows 用户：**
   ```bash
   deploy-to-github.bat
   ```

   **Linux/Mac 用户：**
   ```bash
   chmod +x deploy-to-github.sh
   ./deploy-to-github.sh
   ```

2. **触发构建**

   推送代码后，GitHub Actions 会自动开始构建。

3. **下载 APK**

   - 访问 GitHub 仓库的 Actions 页面
   - 选择最近的工作流运行
   - 在 "Artifacts" 部分下载 APK 文件

#### 详细的 GitHub Actions 使用说明

请查看 [GITHUB_ACTIONS_GUIDE.md](GITHUB_ACTIONS_GUIDE.md) 了解：
- 如何配置签名密钥
- 如何手动触发构建
- 如何通过标签创建 Release
- 常见问题解答

### 方式二：本地编译

#### 环境要求

- Android Studio Arctic Fox 或更高版本
- JDK 8 或更高版本
- Android SDK API 34

#### 编译步骤

1. 使用 Android Studio 打开项目
2. 等待 Gradle 同步完成
3. 点击 Build -> Build Bundle(s) / APK(s) -> Build APK(s)
4. 编译完成后，APK 文件位于 `app/build/outputs/apk/` 目录

#### 生成 Release 版本

1. 在 `app/build.gradle` 中配置签名信息
2. 点击 Build -> Generate Signed Bundle / APK
3. 选择 APK，按照提示完成签名配置
4. 选择 release 构建类型
5. 生成签名的 APK 文件

### 构建状态

![Android CI](https://github.com/huxuyf/verification-code-app/actions/workflows/build-android.yml/badge.svg)

## 注意事项

1. **权限授予**：首次使用必须授予所有必需权限，否则应用无法正常工作
2. **省电模式**：部分厂商的省电模式可能会限制后台服务，建议将应用添加到白名单
3. **短信过滤**：应用只会处理包含"验证码"或"密码"关键词的短信
4. **隐私安全**：应用不会收集、上传任何短信内容，所有数据仅在本地处理

## 常见问题

### Q: 为什么收到的验证码没有弹窗？

A: 请检查以下几点：
- 确认短信内容包含"验证码"或"密码"关键词
- 确认已授予所有必需权限
- 确认监听服务已启动
- 检查系统省电模式是否限制了后台服务

### Q: 悬浮窗无法显示？

A: 请确认已授予"在其他应用上层显示"权限，并在系统设置中检查悬浮窗权限是否开启。

### Q: 如何停止监听？

A: 在主界面点击"停止监听"按钮即可停止监听服务。

### Q: 应用会消耗很多电量吗？

A: 应用采用轻量化的监听机制，只在收到短信时进行处理，不会持续占用CPU，电量消耗极低。

## 技术实现

- **短信监听**：使用 BroadcastReceiver 监听系统短信广播
- **验证码提取**：使用正则表达式提取4-8位连续数字
- **悬浮窗显示**：使用 WindowManager 和 TYPE_APPLICATION_OVERLAY 权限
- **服务保活**：使用前台服务确保监听服务不被系统杀死

## 版本信息

- 版本号：1.0
- 最低 Android 版本：6.0 (API 23)
- 目标 Android 版本：14 (API 34)

## 许可证

本项目仅供学习和个人使用。

## 更新日志

### v1.0 (2024-03-29)
- 初始版本发布
- 实现短信监听功能
- 实现验证码自动提取
- 实现悬浮窗显示
- 实现一键复制功能

## 联系方式

如有问题或建议，请联系开发者。
