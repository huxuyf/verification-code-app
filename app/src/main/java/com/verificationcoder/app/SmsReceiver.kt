package com.verificationcoder.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log

class SmsReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SmsReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            val bundle = intent.extras
            bundle?.let {
                val pdus = it.get("pdus") as? Array<*>
                if (!pdus.isNullOrEmpty()) {
                    val messageBuilder = StringBuilder()

                    // 获取短信格式（Android 23+需要）
                    val format = it.getString("format")

                    pdus.forEach { pdu ->
                        val smsMessage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            // Android 6.0+ 使用新的API
                            SmsMessage.createFromPdu(pdu as? ByteArray, format)
                        } else {
                            // Android 6.0以下使用旧API
                            SmsMessage.createFromPdu(pdu as? ByteArray)
                        }

                        smsMessage?.messageBody?.let { body ->
                            messageBuilder.append(body)
                        }
                    }

                    val messageBody = messageBuilder.toString()

                    Log.d(TAG, "收到短信: $messageBody")

                    // 启动服务处理短信
                    val serviceIntent = Intent(context, SmsListenerService::class.java).apply {
                        putExtra("sms_body", messageBody)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                }
            }
        }
    }
}
