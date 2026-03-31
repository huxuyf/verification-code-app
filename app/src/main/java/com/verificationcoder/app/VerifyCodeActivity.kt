package com.verificationcoder.app

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class VerifyCodeActivity : Activity() {

    private var verifyCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置窗口标志，确保可以在任何界面显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        // 添加额外标志确保在其他应用之上显示
        window.addFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        setContentView(R.layout.activity_verify_code)

        // 获取传递过来的验证码
        verifyCode = intent.getStringExtra("code")
        val tvCode = findViewById<TextView>(R.id.tv_code)
        tvCode.text = verifyCode

        // --- 复制按钮逻辑 ---
        val btnCopy = findViewById<Button>(R.id.btn_copy)
        btnCopy.setOnClickListener {
            if (!verifyCode.isNullOrEmpty()) {
                copyToClipboard(verifyCode!!)
                Toast.makeText(this, "验证码已复制到剪贴板", Toast.LENGTH_SHORT).show()
                // 复制完可以选择自动关闭弹窗，或者不关闭让用户自己关
                finish()
            }
        }

        // --- 关闭按钮逻辑 ---
        val btnClose = findViewById<Button>(R.id.btn_close)
        btnClose.setOnClickListener { finish() }
    }

    /**
     * 兼容所有安卓版本的复制方法
     */
    private fun copyToClipboard(text: String) {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboardManager?.setPrimaryClip(ClipData.newPlainText("verify_code", text))
    }
}
