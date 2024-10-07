package com.lf.magpie_bridge_js.api

import com.lf.magpie_bridge_js.model.MagpieContext

interface IMagpieBridgeDispatcher {
    fun handlePush(context: MagpieContext, cmd: String, args: String)

    fun handleSyncRequest(context: MagpieContext, cmd: String, args: String): String

    fun handleAsyncRequest(context: MagpieContext, cmd: String, args: String, callbackCmd: String)
}