package com.verificationcoder.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 1002;

    private TextView tvStatus;
    private Button btnRequestPermissions;
    private Button btnStartService;
    private Button btnStopService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        updatePermissionStatus();
    }

    private void initViews() {
        tvStatus = findViewById(R.id.tvStatus);
        btnRequestPermissions = findViewById(R.id.btnRequestPermissions);
        btnStartService = findViewById(R.id.btnStartService);
        btnStopService = findViewById(R.id.btnStopService);

        btnRequestPermissions.setOnClickListener(v -> requestAllPermissions());
        btnStartService.setOnClickListener(v -> startSmsListenerService());
        btnStopService.setOnClickListener(v -> stopSmsListenerService());
    }

    private void updatePermissionStatus() {
        boolean hasSmsPermission = checkSmsPermission();
        boolean hasOverlayPermission = checkOverlayPermission();

        StringBuilder status = new StringBuilder();
        status.append("权限状态:\n\n");
        status.append("短信权限: ").append(hasSmsPermission ? "✓ 已授予" : "✗ 未授予").append("\n");
        status.append("悬浮窗权限: ").append(hasOverlayPermission ? "✓ 已授予" : "✗ 未授予").append("\n\n");

        if (hasSmsPermission && hasOverlayPermission) {
            status.append("所有权限已授予，可以开始使用！");
            btnRequestPermissions.setEnabled(false);
            btnStartService.setEnabled(true);
        } else {
            status.append("请授予所有权限后开始使用");
            btnRequestPermissions.setEnabled(true);
            btnStartService.setEnabled(false);
        }

        tvStatus.setText(status.toString());
    }

    private boolean checkSmsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }

    private void requestAllPermissions() {
        // 先请求短信权限
        String[] smsPermissions = {
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS
        };
        ActivityCompat.requestPermissions(this, smsPermissions, PERMISSION_REQUEST_CODE);
    }

    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
        }
    }

    private void startSmsListenerService() {
        if (!checkSmsPermission() || !checkOverlayPermission()) {
            Toast.makeText(this, "请先授予所有权限", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent serviceIntent = new Intent(this, SmsListenerService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        Toast.makeText(this, "监听服务已启动", Toast.LENGTH_SHORT).show();
        btnStartService.setEnabled(false);
        btnStopService.setEnabled(true);
    }

    private void stopSmsListenerService() {
        Intent serviceIntent = new Intent(this, SmsListenerService.class);
        stopService(serviceIntent);

        Toast.makeText(this, "监听服务已停止", Toast.LENGTH_SHORT).show();
        btnStartService.setEnabled(true);
        btnStopService.setEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Toast.makeText(this, "短信权限已授予", Toast.LENGTH_SHORT).show();
                // 检查悬浮窗权限
                if (!checkOverlayPermission()) {
                    requestOverlayPermission();
                } else {
                    updatePermissionStatus();
                }
            } else {
                Toast.makeText(this, "请授予短信权限", Toast.LENGTH_SHORT).show();
                updatePermissionStatus();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (checkOverlayPermission()) {
                Toast.makeText(this, "悬浮窗权限已授予", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "请授予悬浮窗权限", Toast.LENGTH_SHORT).show();
            }
            updatePermissionStatus();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePermissionStatus();
    }
}
