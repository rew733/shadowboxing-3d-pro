package com.example.boxingcoachpro; // (استبدل باسم حزمة مشروعك الفعلي)

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_CODE = 100;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // تأكد من مطابقة ملف الـ layout لديك

        // 1. طلب إذن الكاميرا من نظام أندرويد وقت التشغيل (Runtime Permission)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }

        webView = findViewById(R.id.webview); // تأكد أن معرف الـ WebView في ملف الـ XML هو webview
        WebSettings webSettings = webView.getSettings();
        
        // تفعيل خصائص الـ JavaScript والبيانات المحلية لتشغيل Three.js و MediaPipe بسلاسة
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);

        // 2. الحل الجذري لشاشة الكاميرا السوداء: السماح التلقائي لطلبات الكاميرا في الـ WebView
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // منح جميع الأذونات المطلوبة (مثل الكاميرا) لصفحة الـ HTML داخل الـ WebView
                        request.grant(request.getResources());
                    }
                });
            }
        });

        // 3. تحميل ملف index.html الموجود في مجلد assets
        webView.loadUrl("file:///android_asset/index.html");
    }

    // استقبال نتيجة نافذة طلب إذن الكاميرا من المستخدم
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // إذا وافق المستخدم، قم بتحديث أو إعادة تحميل الـ WebView لتفعيل الكاميرا فوراً
                if (webView != null) {
                    webView.reload();
                }
            }
        }
    }
}
