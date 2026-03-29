package com.verificationcoder.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class FloatingWindowManager {

    private static FloatingWindowManager instance;
    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams params;
    private String currentVerificationCode;

    private FloatingWindowManager() {
    }

    public static synchronized FloatingWindowManager getInstance() {
        if (instance == null) {
            instance = new FloatingWindowManager();
        }
        return instance;
    }

    public void showFloatingWindow(Context context, String verificationCode) {
        if (windowManager == null) {
            windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }

        // 如果已经有悬浮窗显示，先关闭
        if (floatingView != null) {
            closeFloatingWindow();
        }

        currentVerificationCode = verificationCode;

        // 检查悬浮窗权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            Toast.makeText(context, "请授予悬浮窗权限", Toast.LENGTH_LONG).show();
            // 跳转到设置页面
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + context.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return;
        }

        try {
            // 创建悬浮窗视图
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            floatingView = inflater.inflate(R.layout.floating_window, null);

            // 设置验证码文本
            TextView tvCode = floatingView.findViewById(R.id.tvVerificationCode);
            tvCode.setText(verificationCode);

            // 设置按钮事件
            Button btnCopy = floatingView.findViewById(R.id.btnCopy);
            btnCopy.setOnClickListener(v -> {
                copyToClipboard(context, verificationCode);
                Toast.makeText(context, "验证码已复制", Toast.LENGTH_SHORT).show();
                closeFloatingWindow();
            });

            TextView btnClose = floatingView.findViewById(R.id.btnClose);
            btnClose.setOnClickListener(v -> closeFloatingWindow());

            // 设置悬浮窗参数
            int flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;

            // Android 12+ 需要添加额外的标志
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flags |= WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
            }

            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                            WindowManager.LayoutParams.TYPE_PHONE,
                    flags,
                    PixelFormat.TRANSLUCENT);

            params.gravity = Gravity.CENTER;
            params.x = 0;
            params.y = 0;

            // 确保悬浮窗在所有应用之上
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                params.type = WindowManager.LayoutParams.TYPE_PHONE;
            }

            // 设置布局参数以确保在锁屏和后台也能显示
            params.format = PixelFormat.TRANSLUCENT;
            params.windowAnimations = android.R.style.Animation_Dialog;

            // 添加悬浮窗
            windowManager.addView(floatingView, params);

            // 添加拖动功能
            addDragListener();

            android.util.Log.d("FloatingWindow", "悬浮窗显示成功");

        } catch (Exception e) {
            android.util.Log.e("FloatingWindow", "显示悬浮窗失败", e);
            Toast.makeText(context, "显示悬浮窗失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void addDragListener() {
        floatingView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private float dx;
            private float dy;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        dx = event.getRawX() - initialTouchX;
                        dy = event.getRawY() - initialTouchY;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingView, params);
                        return true;

                    case MotionEvent.ACTION_UP:
                        // 如果移动距离很小，认为是点击事件，返回false让按钮处理
                        float moveDistance = (float) Math.sqrt(
                                Math.pow(event.getRawX() - initialTouchX, 2) +
                                Math.pow(event.getRawY() - initialTouchY, 2)
                        );
                        if (moveDistance < 10) {
                            return false;
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void copyToClipboard(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("验证码", text);
        clipboard.setPrimaryClip(clip);
    }

    public void closeFloatingWindow() {
        if (windowManager != null && floatingView != null) {
            windowManager.removeView(floatingView);
            floatingView = null;
        }
    }

    public boolean isShowing() {
        return floatingView != null;
    }
}
