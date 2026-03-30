package com.verificationcoder.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

public class SmsListenerService extends Service {

    private static final String TAG = "SmsListenerService";
    private static final String CHANNEL_ID = "SmsListenerChannel";
    private static final String CHANNEL_NAME = "短信监听服务";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1, createNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("sms_body")) {
            String smsBody = intent.getStringExtra("sms_body");
            processSms(smsBody);
        }
        // 使用 START_STICKY 确保服务被杀死后自动重启
        return START_STICKY;
    }

    private void processSms(String smsBody) {
        Log.d(TAG, "处理短信: " + smsBody);

        // 检查是否包含关键词
        if (smsBody.contains("密码") || smsBody.contains("验证码")) {
            String verificationCode = VerificationCodeExtractor.extractCode(smsBody);
            if (verificationCode != null && !verificationCode.isEmpty()) {
                Log.d(TAG, "提取到验证码: " + verificationCode);
                // 启动验证码显示Activity，可以在任何界面显示
                Intent intent = new Intent(this, VerifyCodeActivity.class);
                intent.putExtra("code", verificationCode);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } else {
                Log.d(TAG, "未找到验证码");
            }
        } else {
            Log.d(TAG, "短信不包含关键词");
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("短信验证码监听服务");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }
        builder.setContentTitle("验证码监听服务")
                .setContentText("正在监听短信验证码...")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(Notification.PRIORITY_LOW);

        return builder.build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
