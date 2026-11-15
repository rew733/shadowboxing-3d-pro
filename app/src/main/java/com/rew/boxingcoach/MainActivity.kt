package com.rew.boxingcoach

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
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
            // 1. إنشاء الحاوية برمجياً لتجنب أي أعطال متعلقة بالواجهة
            webView = WebView(this).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            setContentView(webView)

            // 2. إعدادات المتصفح اللازمة لتشغيل الكاميرا دون تعارض
            val webSettings: WebSettings = webView.settings
            webSettings.javaScriptEnabled = true
            webSettings.domStorageEnabled = true
            webSettings.mediaPlaybackRequiresUserGesture = false // يسمح بفتح الكاميرا فوراً بدون نقر إضافي
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
                // إذا لم يكن الإذن ممنوحاً، نطلبه (ولا نحمل الصفحة الآن لمنع التعارض)
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
            } else {
                // إذا كان الإذن ممنوحاً مسبقاً، نحمل الصفحة بأمان
                webView.loadUrl("file:///android_asset/index.html")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 5. هذه الدالة تعمل فور رد المستخدم على طلب الصلاحية
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            // سواء وافق المستخدم أو رفض، نقوم بتحميل الواجهة الآن
            if (::webView.isInitialized) {
                webView.loadUrl("file:///android_asset/index.html")
            }
        }
    }
}
