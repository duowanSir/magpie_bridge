package com.lf.magpie_bridge_js

import android.content.Context
import android.webkit.WebView

interface IBridgeWebContainer: IMagpieBridgeN2J {
    fun getContext(): Context

    fun getWebView(): WebView

    override fun pushEvent(eventType: String, args: String) {
        val webView = getWebView()
        webView.evaluateJavascript()
    }
}