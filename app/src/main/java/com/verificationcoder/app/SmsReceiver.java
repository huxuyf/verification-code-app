package com.verificationcoder.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null &&
                intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {

            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null && pdus.length > 0) {
                    StringBuilder messageBuilder = new StringBuilder();

                    for (Object pdu : pdus) {
                        SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                        if (smsMessage != null) {
                            messageBuilder.append(smsMessage.getMessageBody());
                        }
                    }

                    String messageBody = messageBuilder.toString();

                    Log.d(TAG, "收到短信: " + messageBody);

                    // 启动服务处理短信
                    Intent serviceIntent = new Intent(context, SmsListenerService.class);
                    serviceIntent.putExtra("sms_body", messageBody);
                    context.startService(serviceIntent);
                }
            }
        }
    }
}
