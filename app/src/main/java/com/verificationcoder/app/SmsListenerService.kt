import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
        private const val ACTION_NEW_CODE = "com.verificationcoder.ACTION_NEW_CODE"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID_SERVICE, createServiceNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getStringExtra("sms_body")?.let { smsBody ->
            processSms(smsBody)
        }
        return START_STICKY
    }

    private fun processSms(smsBody: String) {
        Log.d(TAG, "处理短信: $smsBody")

        if (smsBody.contains("密码") || smsBody.contains("验证码")) {
            val verificationCode = VerificationCodeExtractor.extractCode(smsBody)
            if (!verificationCode.isNullOrEmpty()) {
                Log.d(TAG, "提取到验证码: $verificationCode")
                
                // 1. 自动写入剪贴板
                copyToClipboard(verificationCode)
                
                // 2. 发送广播通知 MainActivity 更新 UI
                val broadcastIntent = Intent(ACTION_NEW_CODE).apply {
                    putExtra("code", verificationCode)
                    setPackage(packageName) // 确保只有本应用能收到
                }
                sendBroadcast(broadcastIntent)
                
            }
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("验证码", text)
        clipboard.setPrimaryClip(clip)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createServiceNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            Notification.Builder(this)
        }
        return builder
            .setContentTitle("验证码监听运行中")
            .setContentText("正在自动提取并复制短信验证码")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(Notification.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
