package com.example.tryggakampus.presentation.game

import android.annotation.SuppressLint
import android.os.Build
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ScratchGamePage(modifier: Modifier = Modifier) {
    // Snapshot the current Firebase user (auth state already observed in Navigation.kt)
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"

    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    BackHandler(enabled = (webViewRef?.canGoBack() == true)) {
        webViewRef?.goBack()
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                webViewRef = this
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                // Settings for local Scratch bundle
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                @Suppress("DEPRECATION")
                settings.allowFileAccessFromFileURLs = true
                @Suppress("DEPRECATION")
                settings.allowUniversalAccessFromFileURLs = true
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }

                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        // Provide UID to the page after load if needed
                        evaluateJavascript("window.__FIREBASE_UID__ = '$uid';", null)

                        // Inject persistence for the 'hunger' variable using localStorage.
                        val js = (
                            "(function(){" +
                                "try{" +
                                    "var KEY='tk_game_hunger_'+(window.__FIREBASE_UID__||'anon');" +
                                    "function applySaved(){var s=localStorage.getItem(KEY); if(s!==null){var v=parseInt(s,10); if(!isNaN(v)) window.hunger=v;}}" +
                                    // Apply immediately and again after short delays to survive game init overwrites
                                    "applySaved(); setTimeout(applySaved,500); setTimeout(applySaved,1500);" +
                                    // Periodically persist current hunger
                                    "if(!window.__hungerSaver){" +
                                        "window.__hungerSaver=setInterval(function(){" +
                                            "try{ if(typeof window.hunger!=='undefined'){ localStorage.setItem(KEY, String(window.hunger)); } }catch(e){}" +
                                        "},1500);" +
                                    "}" +
                                "}catch(e){}" +
                            "})();"
                        )
                        evaluateJavascript(js, null)
                    }
                }

                // Expose Android <-> JS bridge
                addJavascriptInterface(GameBridge(uid), "Android")

                // Load the entry point
                loadUrl("file:///android_asset/scratch_game/scratch.html")
            }
        },
        update = { /* no-op */ },
        onRelease = { webViewRef = null }
    )
}

private class GameBridge(private val uid: String) {

    private val db by lazy { FirebaseFirestore.getInstance() }

    /** Called from JS: Android.postScore(123) */
    @JavascriptInterface
    fun postScore(score: Int) {
        // Fire-and-forget write to Firestore
        val data = hashMapOf(
            "uid" to uid,
            "score" to score,
            "ts" to com.google.firebase.Timestamp.now()
        )
        db.collection("game_scores").add(data)
    }

    /** Called from JS to fetch the best score; returns JSON string */
    @JavascriptInterface
    fun getBestScoreJson(): String {
        // NOTE: @JavascriptInterface methods must be sync; keep it simple.
        return """{"uid":"$uid"}"""
    }

    /** Expose UID if page prefers to read it */
    @JavascriptInterface
    fun getUserId(): String = uid
}
