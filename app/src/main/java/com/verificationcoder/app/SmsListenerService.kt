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
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification())
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
                // 启动验证码显示Activity，可以在任何界面显示
                val intent = Intent(this, VerifyCodeActivity::class.java).apply {
                    putExtra("code", verificationCode)
                    // 添加多个标志确保可以在任何界面显示
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                    )
                }
                startActivity(intent)
            } else {
                Log.d(TAG, "未找到验证码")
            }
        } else {
            Log.d(TAG, "短信不包含关键词")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "短信验证码监听服务"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
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
