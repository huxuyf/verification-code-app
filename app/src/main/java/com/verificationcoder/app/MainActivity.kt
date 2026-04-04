package com.verificationcoder.app

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tvCode: TextView
    private lateinit var tvHint: TextView
    private lateinit var llCodeContainer: LinearLayout
    private lateinit var tvCustomToast: TextView
    
    private var lastCode: String? = null
    private val handler = Handler(Looper.getMainLooper())

    private val smsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            startSmsService()
        } else {
            showPermissionGuide()
        }
    }

    private val codeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val code = intent.getStringExtra("code")
            if (!code.isNullOrEmpty()) {
                updateCodeUi(code)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        checkAndRequestPermissions()
        
        // 注册广播监听新验证码
        val filter = IntentFilter("com.verificationcoder.ACTION_NEW_CODE")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(codeReceiver, filter, RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(codeReceiver, filter)
        }
    }

    private fun initViews() {
        tvCode = findViewById(R.id.tvCode)
        tvHint = findViewById(R.id.tvHint)
        llCodeContainer = findViewById(R.id.llCodeContainer)
        tvCustomToast = findViewById(R.id.tvCustomToast)

        llCodeContainer.setOnClickListener {
            lastCode?.let {
                copyToClipboard(it)
                showFastToast()
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            startSmsService()
        } else {
            smsPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    private fun startSmsService() {
        val serviceIntent = Intent(this, SmsListenerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun showPermissionGuide() {
        Toast.makeText(this, "需要短信权限才能自动提取验证码，请在设置中开启", Toast.LENGTH_LONG).show()
        handler.postDelayed({
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        }, 2000)
    }

    private fun updateCodeUi(code: String) {
        lastCode = code
        tvCode.text = code
        tvHint.text = "最新验证码已自动复制"
        // 自动收到时也抖动一下或显示提示？
        showFastToast()
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("验证码", text)
        clipboard.setPrimaryClip(clip)
    }

    private fun showFastToast() {
        tvCustomToast.visibility = View.VISIBLE
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({
            tvCustomToast.visibility = View.GONE
        }, 500) // 0.5秒后消失
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(codeReceiver)
    }

    override fun onResume() {
        super.onResume()
        // 每次回到前台静默检查下权限，如果补齐了就启动服务
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED) {
            startSmsService()
        }
    }
}
