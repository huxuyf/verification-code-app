# 安卓验证码短信弹窗助手 (沉浸式重构版)

一款极致简约、全自动的安卓验证码接收利器。通过实时监听短信，自动提取验证码并完成剪贴板同步，配合沉浸式大字体展示，让验证码接收变得顺滑且无感。

---

## 🚀 核心特性

- **✨ 沉浸式展示**：摒弃传统弹窗，采用主界面 **50sp 超大字体** 居中展示，极速识别。
- **📋 全自动复制**：监听到验证码后，后台静默将其写入系统剪贴板，收到即粘贴。
- **⚡ 极速交互**：
    - 点击展示区可再次手动复制。
    - 伴随 **0.5s 精简提示**（Toast），无干扰反馈。
- **🔒 安全与隐私**：
    - **零权限冗余**：仅需短信与通知权限，已移除悬浮窗及其他敏感权限。
    - **本地处理**：验证码提取完全在手机本地完成，不请求网络，不上传数据。
- **🛠️ 自动化管理**：
    - **启动即激活**：App 打开时自动申请权限，就绪后秒开监听服务。
    - **后台保活**：前台服务确保在系统后台持续稳定运行。

---

## 🛠️ 技术规格

- **开发语言**: Kotlin (1.9.20+)
- **系统要求**: Android 13 (API 33) 及以上（向下兼容需自行调整）
- **构建工具**: Gradle 8.2.2, JDK 17
- **权限需求**:
    - `RECEIVE_SMS` / `READ_SMS` (必选)
    - `POST_NOTIFICATIONS` (Android 13+ 必选)
    - `FOREGROUND_SERVICE` (保活必选)

---

## 📦 构建与发布 (GitHub Actions)

项目已集成完善的自动化构建流程，支持基于 **Git Tag** 的自动发布。

### 1. 触发构建
推送以 `v` 开头的 Tag 即可触发发布流程：
```bash
git tag v2.0.0
git push origin v2.0.0
```

### 2. 自动化流程
- 自动编译生成的 Release APK（支持签名）。
- 自动在 GitHub 上创建 Release 并上传产物。

---

## 🔐 签名与部署指南

为了使 APK 能够正常安装，建议配置签名信息。

### 本地签名 (Keytool)
使用以下命令生成你的签名证书：
```bash
keytool -genkey -v -keystore release.keystore -alias your-alias -keyalg RSA -keysize 2048 -validity 10000
```

### GitHub 配置 (Secrets)
若要利用 GitHub Actions 自动签名，请在仓库 `Settings > Secrets > Actions` 中添加：
1. `KEYSTORE_BASE64`: 证书文件的 Base64 编码字符串。
   - *获取方法*: `[Convert]::ToBase64String([IO.File]::ReadAllBytes("release.keystore"))` (PowerShell)
2. `KEYSTORE_PASSWORD`: 密钥库密码。
3. `KEY_ALIAS`: 密钥别名。
4. `KEY_PASSWORD`: 密钥密码。

---

## 📂 项目结构

```text
verification-code-app/
├── app/src/main/
│   ├── java/.../app/
│   │   ├── MainActivity.kt                # 沉浸式展示与权限引导
│   │   ├── SmsListenerService.kt          # 后台监听与自动复制服务
│   │   ├── SmsReceiver.kt                 # 短信广播接收器
│   │   └── VerificationCodeExtractor.kt   # 验证码正则提取逻辑
│   ├── res/layout/
│   │   └── activity_main.xml              # 极简大字体布局
│   └── AndroidManifest.xml                # 系统清单（极简权限声明）
├── .github/workflows/                     # GitHub Actions (Release/Build)
└── build.gradle                           # 构建配置 (SDK 34)
```

---

## 📜 更新日志

### v2.0.0 (2026-04-04) - **全案沉浸式重构**
- **UI 革命**：移除所有按钮与复杂界面，改为大字体居中渲染模式。
- **逻辑自动化**：实现静默自动复制验证码至剪贴板。
- **架构瘦身**：彻底删除 `VerifyCodeActivity` 和 `FloatingWindowManager`（悬浮窗逻辑）。
- **权限精简**：移除 `SYSTEM_ALERT_WINDOW` 和 `USE_FULL_SCREEN_INTENT` 权限。
- **发布优化**：GitHub Actions 改为由 Tag 触发，不再依赖 main 分支提交。

### v1.x (2024-03)
- 初始 Kotlin 版本。
- 实现悬浮窗弹窗模式与权限请求。
- 适配 Android 13 动态权限管理。

---

## ⚖️ 许可证

本项目仅供个人学习与技术研究使用。请在遵守当地法律法规的前提下使用短信监听功能。
