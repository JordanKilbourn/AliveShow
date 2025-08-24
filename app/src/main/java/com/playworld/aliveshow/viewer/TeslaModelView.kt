package com.playworld.aliveshow.viewer

import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun TeslaModelView(
    modifier: Modifier = Modifier,
    amplitude: Float = 0f,
    headlight: Boolean = true,
    drl: Boolean = true,
    fog: Boolean = false,
    tail: Boolean = true
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.cacheMode = WebSettings.LOAD_NO_CACHE
                webChromeClient = WebChromeClient()
                loadUrl("file:///android_asset/tesla_viewer.html")
            }
        },
        update = { wv ->
            val js = "javascript:setLights(" +
                    (if (headlight) 1 else 0) + "," +
                    (if (drl) 1 else 0) + "," +
                    (if (fog) 1 else 0) + "," +
                    (if (tail) 1 else 0) + "," +
                    String.format("%.3f", amplitude.coerceIn(0f, 1f)) +
                    ")"
            wv.evaluateJavascript(js, null)
        }
    )
}
