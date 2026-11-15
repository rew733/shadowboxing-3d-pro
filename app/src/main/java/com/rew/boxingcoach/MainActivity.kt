package com.rew.boxingcoach

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : Activity() {
    private val CAMERA_PERMISSION_CODE = 100
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // 1. إنشاء حاوية المتصفح وتفعيل تسريع العتاد الصلب (GPU Layer)
            webView = WebView(this).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
            }
            setContentView(webView)

            // 2. إعدادات المتصفح اللازمة لتشغيل الكاميرا والذكاء الاصطناعي بأقصى أداء
            val webSettings: WebSettings = webView.settings
            webSettings.javaScriptEnabled = true
            webSettings.domStorageEnabled = true
            webSettings.mediaPlaybackRequiresUserGesture = false
            webSettings.allowFileAccess = true
            webSettings.allowContentAccess = true
            webSettings.allowFileAccessFromFileURLs = true
            webSettings.allowUniversalAccessFromFileURLs = true

            // 3. إدارة إذن الكاميرا داخل المتصفح
            webView.webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest) {
                    runOnUiThread {
                        request.grant(request.resources)
                    }
                }
            }

            // 4. التحقق من إذن الكاميرا في الأندرويد قبل تحميل الصفحة
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
            } else {
                webView.loadUrl("file:///android_asset/index.html")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 5. تحميل الصفحة فور رد المستخدم على طلب الصلاحية
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (::webView.isInitialized) {
                webView.loadUrl("file:///android_asset/index.html")
            }
        }
    }
}
