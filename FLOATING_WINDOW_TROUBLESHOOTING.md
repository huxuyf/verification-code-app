# 悬浮窗后台显示问题修复说明

## 🎯 问题描述

**问题**: 悬浮窗只在应用界面打开时才能看到，应用最小化到后台时悬浮窗不可见。

**原因**: 悬浮窗的显示参数配置不当，缺少必要的标志来确保在后台也能显示。

---

## ✅ 已完成的修复

### 1. 优化悬浮窗显示参数

**修改文件**: `FloatingWindowManager.java`

**关键修改**:
```java
// 添加了更多的显示标志
int flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;

// Android 12+ 需要添加额外的标志
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    flags |= WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
}
```

**标志说明**:
- `FLAG_NOT_FOCUSABLE`: 悬浮窗不获取焦点，不影响其他应用
- `FLAG_NOT_TOUCH_MODAL`: 允许点击悬浮窗外部区域
- `FLAG_LAYOUT_IN_SCREEN`: 悬浮窗可以在屏幕边界之外
- `FLAG_LAYOUT_NO_LIMITS`: 移除布局限制
- `FLAG_HARDWARE_ACCELERATED`: 启用硬件加速，提高性能
- `FLAG_LAYOUT_INSET_DECOR`: Android 12+ 特殊标志

### 2. 优化服务配置

**修改文件**: `SmsListenerService.java`

**关键修改**:
```java
// 修改返回值，确保服务被杀死后自动重启
return START_STICKY;
```

**说明**:
- `START_STICKY`: 服务被杀死后会自动重启
- 确保短信监听服务持续运行

### 3. 添加错误处理和日志

**修改文件**: `FloatingWindowManager.java`

**新增功能**:
- 添加 try-catch 异常处理
- 添加详细的日志输出
- 错误时显示 Toast 提示

---

## 🧪 测试步骤

### 1. 基本测试

1. **启动应用**
   - 打开应用
   - 授予所有权限（短信、悬浮窗、通知）

2. **启动监听服务**
   - 点击"启动服务"按钮
   - 确认通知栏显示"验证码监听服务"

3. **测试短信接收**
   - 使用另一部手机发送包含验证码的短信
   - 确认悬浮窗正常显示

### 2. 后台显示测试

1. **最小化应用**
   - 按返回键或Home键
   - 应用进入后台

2. **接收验证码短信**
   - 发送包含验证码的短信
   - **验证**: 悬浮窗应该在屏幕上显示

3. **切换到其他应用**
   - 打开其他应用（如浏览器、聊天应用）
   - 发送验证码短信
   - **验证**: 悬浮窗应该在当前应用上方显示

### 3. 锁屏测试（可选）

1. **锁定屏幕**
   - 按电源键锁定屏幕

2. **接收验证码短信**
   - 发送验证码短信

3. **解锁屏幕**
   - 解锁后查看

**注意**: 某些设备厂商可能会限制锁屏时的悬浮窗显示。

---

## 🔍 故障排查

### 问题 1: 后台仍然看不到悬浮窗

**可能原因**:
1. 悬浮窗权限被撤销
2. 系统省电策略限制了后台服务
3. 厂商定制系统限制了悬浮窗

**解决方案**:

1. **检查权限**:
   - 进入设置 → 应用管理 → 验证码助手
   - 确认"显示在其他应用上层"权限已开启

2. **关闭省电优化**:
   - 设置 → 电池 → 应用省电策略
   - 找到"验证码助手" → 选择"不限制"

3. **允许后台运行**:
   - 设置 → 应用管理 → 验证码助手
   - 电池 → 允许后台活动

4. **厂商特殊设置** (针对小米、华为、OPPO等):

   **小米**:
   - 设置 → 应用设置 → 应用管理 → 验证码助手
   - 权限管理 → 显示悬浮窗 → 允许
   - 自启动管理 → 允许

   **华为**:
   - 手机管家 → 应用启动管理 → 验证码助手
   - 关闭"自动管理"
   - 允许后台活动

   **OPPO**:
   - 设置 → 应用管理 → 验证码助手
   - 权限管理 → 悬浮窗 → 允许
   - 自启动管理 → 允许

### 问题 2: 悬浮窗显示但无法点击

**可能原因**: 悬浮窗被其他应用遮挡

**解决方案**:
- 拖动悬浮窗到其他位置
- 检查是否有其他全屏应用覆盖

### 问题 3: 服务自动停止

**可能原因**:
1. 系统内存不足
2. 省电策略限制
3. 应用被系统杀死

**解决方案**:
1. **查看日志**:
   ```bash
   adb logcat | grep SmsListenerService
   adb logcat | grep FloatingWindow
   ```

2. **确保服务持续运行**:
   - 不要手动清除后台应用
   - 将应用加入白名单

3. **检查通知**:
   - 确认通知栏有"验证码监听服务"通知
   - 如果通知消失，说明服务被停止

---

## 📱 不同Android版本的注意事项

### Android 13 (API 33)

- ✅ 完全支持
- 需要通知权限
- 悬浮窗权限需要用户手动授予

### Android 12 (API 31-32)

- ✅ 完全支持
- 需要添加 `FLAG_LAYOUT_INSET_DECOR` 标志
- 某些厂商可能有限制

### Android 11 及以下

- ✅ 完全支持
- 使用 `TYPE_PHONE` 而不是 `TYPE_APPLICATION_OVERLAY`

---

## 🛠️ 开发者调试

### 查看日志

```bash
# 查看悬浮窗相关日志
adb logcat | grep FloatingWindow

# 查看服务相关日志
adb logcat | grep SmsListenerService

# 查看短信接收日志
adb logcat | grep SmsReceiver
```

### 检查权限状态

```bash
# 检查悬浮窗权限
adb shell dumpsys package com.verificationcoder.app | grep overlay

# 检查短信权限
adb shell dumpsys package com.verificationcoder.app | grep sms
```

### 手动测试悬浮窗

在 MainActivity 中添加测试按钮：
```java
Button btnTest = findViewById(R.id.btnTest);
btnTest.setOnClickListener(v -> {
    FloatingWindowManager.getInstance().showFloatingWindow(this, "123456");
});
```

---

## 📊 预期行为

修复后，悬浮窗应该能够：

✅ 在应用前台时正常显示
✅ 在应用后台时正常显示
✅ 在其他应用上层显示
✅ 在锁屏解锁后显示（部分设备）
✅ 不影响其他应用的正常使用

---

## 🔧 技术细节

### 悬浮窗类型

- **Android 8.0+**: `TYPE_APPLICATION_OVERLAY`
- **Android 8.0以下**: `TYPE_PHONE`

### 服务模式

- **前台服务**: 使用通知栏保持服务运行
- **START_STICKY**: 服务被杀死后自动重启

### 权限要求

- `SYSTEM_ALERT_WINDOW`: 悬浮窗权限
- `RECEIVE_SMS`: 接收短信权限
- `READ_SMS`: 读取短信权限
- `POST_NOTIFICATIONS`: 通知权限（Android 13+）

---

## 📝 总结

本次修复主要解决了以下问题：

1. ✅ 悬浮窗在后台不可见
2. ✅ 服务被系统杀死后不自动重启
3. ✅ 缺少错误处理和日志

**关键改进**:
- 添加了更多的悬浮窗显示标志
- 修改服务为 `START_STICKY` 模式
- 添加了完善的错误处理
- 增加了详细的日志输出

现在悬浮窗应该能够在应用后台正常显示了！

---

**最后更新**: 2025-01-XX
**版本**: 1.0
