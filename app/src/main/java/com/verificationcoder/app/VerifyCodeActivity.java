package com.verificationcoder.app;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class VerifyCodeActivity extends Activity {

    private String verifyCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 保持屏幕常亮、解锁显示、在锁屏之上显示
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_verify_code);

        // 获取传递过来的验证码
        verifyCode = getIntent().getStringExtra("code");
        TextView tvCode = findViewById(R.id.tv_code);
        tvCode.setText(verifyCode);

        // --- 复制按钮逻辑 ---
        Button btnCopy = findViewById(R.id.btn_copy);
        btnCopy.setOnClickListener(v -> {
            if (verifyCode != null && !verifyCode.isEmpty()) {
                copyToClipboard(verifyCode);
                Toast.makeText(this, "验证码已复制到剪贴板", Toast.LENGTH_SHORT).show();
                // 复制完可以选择自动关闭弹窗，或者不关闭让用户自己关
                finish();
            }
        });

        // --- 关闭按钮逻辑 ---
        Button btnClose = findViewById(R.id.btn_close);
        btnClose.setOnClickListener(v -> finish());
    }

    /**
     * 兼容所有安卓版本的复制方法
     */
    private void copyToClipboard(String text) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager != null) {
            // 安卓 10 (API 29) 及以上，必须使用 ClipData.newPlainText，否则复制无效
            ClipData clipData = ClipData.newPlainText("verify_code", text);
            clipboardManager.setPrimaryClip(clipData);
        }
    }
}
