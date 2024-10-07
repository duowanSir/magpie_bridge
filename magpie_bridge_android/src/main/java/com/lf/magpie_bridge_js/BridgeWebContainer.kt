package com.lf.magpie_bridge_js

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.google.gson.Gson
import com.lf.magpie_bridge_js.api.IMagpieBridgeAsyncHandler
import com.lf.magpie_bridge_js.api.IMagpieBridgeDispatcher
import com.lf.magpie_bridge_js.api.IMagpieBridgeN2J
import com.lf.magpie_bridge_js.api.IMagpieBridgePushHandler
import com.lf.magpie_bridge_js.api.IMagpieBridgeSyncHandler
import com.lf.magpie_bridge_js.model.MagpieContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean


class BridgeWebContainer(
    private val context: Context,
    private val webView: WebView,
) : IMagpieBridgeN2J, IMagpieBridgeDispatcher {

    companion object {
        const val TAG = "BridgeWebContainer"
        const val ERROR_RESULT_NOT_FOUND_COMMANDER = "not_found_commander"

        fun getTimestamp(): String {
            val currentTime = Date()
            val dateFormat = SimpleDateFormat("yy-MM-dd:HH-mm-ss:SSS", Locale.getDefault())
            return dateFormat.format(currentTime)
        }
    }

    private val isJsReady = AtomicBoolean(false)

    private val pushHandlers = HashMap<String, MutableSet<IMagpieBridgePushHandler>>()
    private val syncRequestHandlers = HashMap<String, IMagpieBridgeSyncHandler>()
    private val asyncRequestHandlers = HashMap<String, IMagpieBridgeAsyncHandler>()
    private val innerCallbackHandlers = HashMap<String, (String) -> Unit>()


    init {
        webView.addJavascriptInterface(this, "magpieBridgeAndroid")
    }

    private fun getShortTraceId(): String {
        val uuid = UUID.randomUUID()
        val uuidString = uuid.toString()
        return uuidString.substring(0, 8)
    }

    @JavascriptInterface
    fun onReceivePushFromJs(context: String, cmd: String, args: String) {
        Log.d(TAG, "onReceivePushFromJs context=${context} cmd=$cmd args=$args")
        val gson = Gson()
        val magpieContext = gson.fromJson(context, MagpieContext::class.java)
        handlePush(magpieContext, cmd, args)
    }

    @JavascriptInterface
    fun onReceiveRequestFromJs(
        context: String,
        cmd: String,
        args: String,
        callbackCmd: String
    ): String? {
        val gson = Gson()
        val magpieContext = gson.fromJson(context, MagpieContext::class.java)
        if (callbackCmd.isBlank()) {
            return handleSyncRequest(magpieContext, cmd, args)
        }
        handleAsyncRequest(magpieContext, cmd, args, callbackCmd)
        return null
    }

    override fun handlePush(context: MagpieContext, cmd: String, args: String) {
        if (cmd == BaseCommand.EventJsReady.name) {
            isJsReady.compareAndSet(false, true)
            return
        }
        val pushes = pushHandlers[cmd]
        if (!pushes.isNullOrEmpty()) {
            pushes.forEach() {
                it.handle(context, cmd, args)
            }
            return
        }
        val requestCallbackListener = innerCallbackHandlers[cmd]
        if (requestCallbackListener != null) {
            requestCallbackListener(args)
        }
    }

    override fun handleSyncRequest(context: MagpieContext, cmd: String, args: String): String {
        val handler = syncRequestHandlers[cmd] ?: return ERROR_RESULT_NOT_FOUND_COMMANDER
        return handler.handle(context, cmd, args)
    }

    override fun handleAsyncRequest(
        context: MagpieContext,
        cmd: String,
        args: String,
        callbackCmd: String
    ) {
        val handler = asyncRequestHandlers[cmd]
        if (handler == null) {
            pushRequest(callbackCmd, ERROR_RESULT_NOT_FOUND_COMMANDER)
            return
        }
        handler.handle(context, cmd, args) {
            webView.evaluateJavascript(
                "javascript:onReceivePushFromNative(${callbackCmd}, ${it})",
                null
            )
        }
    }

    override fun addPushHandler(cmd: String, handler: IMagpieBridgePushHandler) {
        val handlers = pushHandlers[cmd] ?: HashSet()
        handlers.add(handler)
        pushHandlers[cmd] = handlers
    }

    override fun registerSyncHandler(cmd: String, handler: IMagpieBridgeSyncHandler) {
        syncRequestHandlers[cmd] = handler
    }

    override fun registerAsyncHandler(cmd: String, handler: IMagpieBridgeAsyncHandler) {
        asyncRequestHandlers[cmd] = handler
    }

    override fun pushRequest(cmd: String, args: String) {
        if (!isJsReady.get()) {
            throw BridgeException("pushEvent eventType=$cmd args=$args failed because jsReady false")
        }
        val magpieContext = MagpieContext(getShortTraceId(), getTimestamp())
        val gson = Gson()
        val contextStr = gson.toJson(magpieContext)
        val jsCmd = "window.onReceivePushFromNative('${contextStr}', '${cmd}', '${args}')"
        Log.d(TAG, "pushRequest jsCmd=$jsCmd")
        webView.evaluateJavascript(
            jsCmd
        ) { value -> Log.d(TAG, "pushRequest onReceiveValue value=$value") }
    }

    override fun syncRequest(cmd: String, args: String, callback: (String) -> Unit) {
        if (!isJsReady.get()) {
            throw BridgeException("syncRequest funcName=$cmd args=$args failed because jsReady false")
        }
        val magpieContext = MagpieContext(getShortTraceId(), getTimestamp())
        val gson = Gson()
        val contextStr = gson.toJson(magpieContext)
        val jsCmd = "window.onReceiveRequestFromNative('${contextStr}', '${cmd}', '${args}', '')"
        Log.d(TAG, "syncRequest jsCmd=$jsCmd")
        webView.evaluateJavascript(
            jsCmd
        ) { value -> callback(value) }
    }

    override fun asyncRequest(cmd: String, args: String, callback: (String) -> Unit) {
        if (!isJsReady.get()) {
            throw BridgeException("asyncRequest funcName=$cmd args=$args failed because jsReady false")
        }
        val uuid = UUID.randomUUID()
        val uuidString = uuid.toString()
        innerCallbackHandlers[uuidString] = callback
        val jsCmd = "window.onReceiveRequestFromNative('${cmd}', '${args}', '${uuidString}')"
        Log.d(TAG, "asyncRequest jsCmd=$jsCmd")
        webView.evaluateJavascript(
            jsCmd,
            null
        )
    }
}