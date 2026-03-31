package com.verificationcoder.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.provider.Settings
import kotlin.math.pow
import kotlin.math.sqrt
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class FloatingWindowManager private constructor() {

    companion object {
        @Volatile
        private var instance: FloatingWindowManager? = null

        fun getInstance(): FloatingWindowManager {
            return instance ?: synchronized(this) {
                instance ?: FloatingWindowManager().also { instance = it }
            }
        }
    }

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var params: WindowManager.LayoutParams? = null
    private var currentVerificationCode: String? = null

    fun showFloatingWindow(context: Context, verificationCode: String) {
        if (windowManager == null) {
            windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        }

        // 如果已经有悬浮窗显示，先关闭
        if (floatingView != null) {
            closeFloatingWindow()
        }

        currentVerificationCode = verificationCode

        // 检查悬浮窗权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            Toast.makeText(context, "请授予悬浮窗权限", Toast.LENGTH_LONG).show()
            // 跳转到设置页面
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return
        }

        try {
            // 创建悬浮窗视图
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            floatingView = inflater.inflate(R.layout.floating_window, null)

            // 设置验证码文本
            val tvCode = floatingView?.findViewById<TextView>(R.id.tvVerificationCode)
            tvCode?.text = verificationCode

            // 设置按钮事件
            val btnCopy = floatingView?.findViewById<Button>(R.id.btnCopy)
            btnCopy?.setOnClickListener {
                copyToClipboard(context, verificationCode)
                Toast.makeText(context, "验证码已复制", Toast.LENGTH_SHORT).show()
                closeFloatingWindow()
            }

            val btnClose = floatingView?.findViewById<TextView>(R.id.btnClose)
            btnClose?.setOnClickListener { closeFloatingWindow() }

            // 设置悬浮窗参数
            var flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED

            // Android 12+ 需要添加额外的标志
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flags = flags or WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
            }

            params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                flags,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
                x = 0
                y = 0

                // 确保悬浮窗在所有应用之上
                type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_PHONE
                }

                // 设置布局参数以确保在锁屏和后台也能显示
                format = PixelFormat.TRANSLUCENT
                windowAnimations = android.R.style.Animation_Dialog
            }

            // 添加悬浮窗
            windowManager?.addView(floatingView, params)

            // 添加拖动功能
            addDragListener()

            android.util.Log.d("FloatingWindow", "悬浮窗显示成功")

        } catch (e: Exception) {
            android.util.Log.e("FloatingWindow", "显示悬浮窗失败", e)
            Toast.makeText(context, "显示悬浮窗失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addDragListener() {
        floatingView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params?.x ?: 0
                        initialY = params?.y ?: 0
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        params?.x = initialX + (event.rawX - initialTouchX).toInt()
                        params?.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager?.updateViewLayout(floatingView, params)
                        return true
                    }

                    MotionEvent.ACTION_UP -> {
                        // 如果移动距离很小，认为是点击事件，返回false让按钮处理
                        val moveDistance = sqrt(
                            pow(event.rawX - initialTouchX, 2.0) +
                                    pow(event.rawY - initialTouchY, 2.0)
                        ).toFloat()
                        if (moveDistance < 10) {
                            return false
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("验证码", text)
        clipboard.setPrimaryClip(clip)
    }

    fun closeFloatingWindow() {
        if (windowManager != null && floatingView != null) {
            windowManager?.removeView(floatingView)
            floatingView = null
        }
    }

    fun isShowing(): Boolean {
        return floatingView != null
    }
}
