package com.verificationcoder.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log

class SmsListenerService : Service() {

    companion object {
        private const val TAG = "SmsListenerService"
        private const val CHANNEL_ID = "SmsListenerChannel"
        private const val CHANNEL_NAME = "短信监听服务"
        private const val NOTIFICATION_ID_SERVICE = 1
        private const val NOTIFICATION_ID_VERIFY = 2
        private const val CHANNEL_ID_VERIFY = "VerifyCodeChannel"
        private const val CHANNEL_NAME_VERIFY = "验证码提醒"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        startForeground(NOTIFICATION_ID_SERVICE, createServiceNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getStringExtra("sms_body")?.let { smsBody ->
            processSms(smsBody)
        }
        // 使用 START_STICKY 确保服务被杀死后自动重启
        return START_STICKY
    }

    private fun processSms(smsBody: String) {
        Log.d(TAG, "处理短信: $smsBody")

        // 检查是否包含关键词
        if (smsBody.contains("密码") || smsBody.contains("验证码")) {
            val verificationCode = VerificationCodeExtractor.extractCode(smsBody)
            if (!verificationCode.isNullOrEmpty()) {
                Log.d(TAG, "提取到验证码: $verificationCode")
                // 1. 尝试启动悬浮窗（原有逻辑保留作为双重保障）
                FloatingWindowManager.getInstance().showFloatingWindow(this, verificationCode)
                
                // 2. 发送全屏意图通知（解决 Android 10+ 后台限制的核心逻辑）
                sendFullScreenNotification(verificationCode)
            } else {
                Log.d(TAG, "未找到验证码")
            }
        } else {
            Log.d(TAG, "短信不包含关键词")
        }
    }

    private fun sendFullScreenNotification(code: String) {
        val intent = Intent(this, VerifyCodeActivity::class.java).apply {
            putExtra("code", code)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = android.app.PendingIntent.getActivity(
            this,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            } else {
                android.app.PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID_VERIFY)
        } else {
            Notification.Builder(this)
        }

        val notification = builder
            .setContentTitle("收到验证码")
            .setContentText("您的验证码是: $code")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(Notification.PRIORITY_MAX)
            .setCategory(Notification.CATEGORY_CALL)
            .setFullScreenIntent(pendingIntent, true) // 关键：全屏意图
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID_VERIFY, notification)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            
            // 服务常驻通知渠道 (Low 优先级，静默)
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            manager?.createNotificationChannel(serviceChannel)

            // 验证码提醒渠道 (High 优先级，允许弹窗)
            val verifyChannel = NotificationChannel(
                CHANNEL_ID_VERIFY,
                CHANNEL_NAME_VERIFY,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "用于在收到验证码时弹出提醒"
                enableLights(true)
            }
            manager?.createNotificationChannel(verifyChannel)
        }
    }

    private fun createServiceNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            Notification.Builder(this)
        }
        return builder
            .setContentTitle("验证码监听服务")
            .setContentText("正在监听短信验证码...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(Notification.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
