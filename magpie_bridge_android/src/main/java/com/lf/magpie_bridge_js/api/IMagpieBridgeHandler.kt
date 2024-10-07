package com.lf.magpie_bridge_js.api

import com.lf.magpie_bridge_js.model.MagpieContext

/**
 * 向bridgeContainer注册的用来处理业务的接口
 * */

interface IMagpieBridgePushHandler {
    fun handle(context: MagpieContext, cmd: String, args: String)
}

// 同步接口
interface IMagpieBridgeSyncHandler {
    fun handle(context: MagpieContext, cmd: String, args: String): String
}

// 异步接口
interface IMagpieBridgeAsyncHandler {
    fun handle(context: MagpieContext, cmd: String, args: String, callback: (String) -> Unit)
}
