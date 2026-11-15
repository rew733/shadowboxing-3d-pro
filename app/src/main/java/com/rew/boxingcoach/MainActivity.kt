package com.rew.boxingcoach

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val CAMERA_PERMISSION_CODE = 100
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // 1. إنشاء الـ WebView برمجياً بالكامل لتجنب الحاجة لأي ملفات XML خارجية
            webView = WebView(this).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            setContentView(webView)

            // 2. طلب إذن الكاميرا وقت التشغيل
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
            }

            // 3. إعدادات الـ WebView والـ Assets المحلية
            val webSettings: WebSettings = webView.settings
            webSettings.javaScriptEnabled = true
            webSettings.domStorageEnabled = true
            webSettings.mediaPlaybackRequiresUserGesture = false
            webSettings.allowFileAccess = true
            webSettings.allowContentAccess = true
            webSettings.allowUniversalAccessFromFileURLs = true // ضروري لتشغيل ملفات الـ Assets والـ Web Worker محلياً

            // 4. السماح التلقائي لطلبات الكاميرا في الـ WebView لمنع الشاشة السوداء
            webView.webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest) {
                    runOnUiThread {
                        request.grant(request.resources)
                    }
                }
            }

            // 5. تحميل واجهة التطبيق
            webView.loadUrl("file:///android_asset/index.html")

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (::webView.isInitialized) {
                    webView.reload()
                }
            }
        }
    }
}
