package com.verificationcoder.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var btnRequestPermissions: Button
    private lateinit var btnStartService: Button
    private lateinit var btnStopService: Button

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        updatePermissionStatus()
        if (checkOverlayPermission()) {
            Toast.makeText(this, "悬浮窗权限已授予", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "请授予悬浮窗权限", Toast.LENGTH_SHORT).show()
        }
    }

    private val smsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Toast.makeText(this, "短信权限已授予", Toast.LENGTH_SHORT).show()
            if (!checkOverlayPermission()) {
                requestOverlayPermission()
            } else {
                updatePermissionStatus()
            }
        } else {
            Toast.makeText(this, "请授予短信权限", Toast.LENGTH_SHORT).show()
            updatePermissionStatus()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        updatePermissionStatus()
    }

    private fun initViews() {
        tvStatus = findViewById(R.id.tvStatus)
        btnRequestPermissions = findViewById(R.id.btnRequestPermissions)
        btnStartService = findViewById(R.id.btnStartService)
        btnStopService = findViewById(R.id.btnStopService)

        btnRequestPermissions.setOnClickListener { requestAllPermissions() }
        btnStartService.setOnClickListener { startSmsListenerService() }
        btnStopService.setOnClickListener { stopSmsListenerService() }
    }

    private fun updatePermissionStatus() {
        val hasSmsPermission = checkSmsPermission()
        val hasOverlayPermission = checkOverlayPermission()

        val status = buildString {
            append("权限状态:\n\n")
            append("短信权限: ${if (hasSmsPermission) "✓ 已授予" else "✗ 未授予"}\n")
            append("悬浮窗权限: ${if (hasOverlayPermission) "✓ 已授予" else "✗ 未授予"}\n\n")

            if (hasSmsPermission && hasOverlayPermission) {
                append("所有权限已授予，可以开始使用！")
                btnRequestPermissions.isEnabled = false
                btnStartService.isEnabled = true
            } else {
                append("请授予所有权限后开始使用")
                btnRequestPermissions.isEnabled = true
                btnStartService.isEnabled = false
            }
        }

        tvStatus.text = status
    }

    private fun checkSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun requestAllPermissions() {
        val smsPermissions = arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
        smsPermissionLauncher.launch(smsPermissions)
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        }
    }

    private fun startSmsListenerService() {
        if (!checkSmsPermission() || !checkOverlayPermission()) {
            Toast.makeText(this, "请先授予所有权限", Toast.LENGTH_SHORT).show()
            return
        }

        val serviceIntent = Intent(this, SmsListenerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        Toast.makeText(this, "监听服务已启动", Toast.LENGTH_SHORT).show()
        btnStartService.isEnabled = false
        btnStopService.isEnabled = true
    }

    private fun stopSmsListenerService() {
        val serviceIntent = Intent(this, SmsListenerService::class.java)
        stopService(serviceIntent)

        Toast.makeText(this, "监听服务已停止", Toast.LENGTH_SHORT).show()
        btnStartService.isEnabled = true
        btnStopService.isEnabled = false
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }
}
