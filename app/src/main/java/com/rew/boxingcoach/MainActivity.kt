package com.rew.boxingcoach

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private val CAMERA_PERMISSION_CODE = 100
    private lateinit var webView: WebView
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // إبقاء شاشة الهاتف مضاءة طوال فترة التدريب لمنع قفل الجهاز أثناء الجولات
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // تهيئة محرك النطق الصوتي للأندرويد
        tts = TextToSpeech(this, this)

        try {
            webView = WebView(this).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
            }
            setContentView(webView)

            val webSettings: WebSettings = webView.settings
            webSettings.javaScriptEnabled = true
            webSettings.domStorageEnabled = true
            webSettings.mediaPlaybackRequiresUserGesture = false
            webSettings.allowFileAccess = true
            webSettings.allowContentAccess = true
            webSettings.allowFileAccessFromFileURLs = true
            webSettings.allowUniversalAccessFromFileURLs = true

            // ربط جسر الصوت AndroidTTS مع الجافاسكربت
            webView.addJavascriptInterface(WebAppInterface(), "AndroidTTS")

            webView.webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest) {
                    runOnUiThread {
                        request.grant(request.resources)
                    }
                }
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
            } else {
                webView.loadUrl("file:///android_asset/index.html")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            isTtsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    inner class WebAppInterface {
        @JavascriptInterface
        fun speak(text: String) {
            if (isTtsReady) {
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "BOXING_CUE")
            }
        }

        @JavascriptInterface
        fun setRate(rate: Float) {
            if (isTtsReady) {
                tts?.setSpeechRate(rate)
            }
        }
    }

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

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
