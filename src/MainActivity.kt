package com.satyam.exifx

import android.app.Activity
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.webkit.WebViewAssetLoader
import java.io.File

class MainActivity : Activity() {

    private lateinit var web: WebView
    private var chooser: ValueCallback<Array<Uri>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = 0xFF050810.toInt()
        window.navigationBarColor = 0xFF050810.toInt()
        askPermissions()
        web = WebView(this)
        setContentView(web)

        val loader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.allowFileAccess = false
        web.addJavascriptInterface(Bridge(), "Android")

        web.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                return if (request == null) null else loader.shouldInterceptRequest(request.url)
            }
        }

        web.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: WebChromeClient.FileChooserParams?
            ): Boolean {
                if (filePathCallback == null || fileChooserParams == null) return false
                chooser?.onReceiveValue(null)
                chooser = filePathCallback
                return try {
                    startActivityForResult(fileChooserParams.createIntent(), 1)
                    true
                } catch (e: Exception) {
                    chooser = null
                    false
                }
            }
        }

        web.setDownloadListener { url, _, _, mime, _ ->
            val type = if (mime.isNullOrBlank()) "application/octet-stream" else mime
            if (url.startsWith("blob:")) {
                val js = "(async()=>{const r=await fetch('" + url + "');const b=await r.blob();" +
                    "const f=new FileReader();f.onloadend=()=>Android.save(f.result.split(',')[1],'" + type + "');" +
                    "f.readAsDataURL(b)})()"
                web.evaluateJavascript(js, null)
            } else if (url.startsWith("data:")) {
                val b64 = url.substringAfter("base64,", "")
                if (b64.isNotEmpty()) saveBytes(Base64.decode(b64, Base64.DEFAULT), type)
            } else {
                try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } catch (e: Exception) {}
            }
        }

        web.loadUrl("https://appassets.androidplatform.net/assets/index.html")
    }

    private fun askPermissions() {
        val list = ArrayList<String>()
        if (Build.VERSION.SDK_INT >= 33) list.add("android.permission.READ_MEDIA_IMAGES")
        else list.add("android.permission.READ_EXTERNAL_STORAGE")
        if (Build.VERSION.SDK_INT >= 29) list.add("android.permission.ACCESS_MEDIA_LOCATION")
        val need = list.filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
        if (need.isNotEmpty()) requestPermissions(need.toTypedArray(), 2)
    }

    private fun original(u: Uri): Uri {
        if (Build.VERSION.SDK_INT < 29) return u
        if (checkSelfPermission("android.permission.ACCESS_MEDIA_LOCATION") != PackageManager.PERMISSION_GRANTED) return u
        return try {
            val m = if (u.authority == "com.android.providers.media.documents") {
                val id = DocumentsContract.getDocumentId(u).substringAfter(":").toLong()
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            } else u
            val o = MediaStore.setRequireOriginal(m)
            contentResolver.openInputStream(o)?.close()
            o
        } catch (e: Exception) {
            u
        }
    }

    inner class Bridge {
        @JavascriptInterface
        fun save(b64: String, mime: String) {
            val bytes = Base64.decode(b64, Base64.DEFAULT)
            runOnUiThread { saveBytes(bytes, mime) }
        }
    }

    private fun ext(mime: String): String = when {
        mime.contains("jpeg") -> "jpg"
        mime.contains("png") -> "png"
        mime.contains("webp") -> "webp"
        mime.contains("json") -> "json"
        mime.contains("text") -> "txt"
        else -> "bin"
    }

    private fun saveBytes(bytes: ByteArray, mime: String) {
        val name = "exifx_" + System.currentTimeMillis() + "." + ext(mime)
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                val v = ContentValues()
                v.put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                v.put(MediaStore.MediaColumns.MIME_TYPE, mime)
                v.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                val u = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v)
                if (u != null) contentResolver.openOutputStream(u)?.use { it.write(bytes) }
                Toast.makeText(this, "Saved in Downloads: " + name, Toast.LENGTH_LONG).show()
            } else {
                val dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                val f = File(dir, name)
                f.writeBytes(bytes)
                Toast.makeText(this, "Saved: " + f.absolutePath, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Save nahi hua: " + e.message, Toast.LENGTH_LONG).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            val res = WebChromeClient.FileChooserParams.parseResult(resultCode, data)
            chooser?.onReceiveValue(res?.map { original(it) }?.toTypedArray())
            chooser = null
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}
