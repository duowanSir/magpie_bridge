package com.lf.magpie_bridge_js.api

/**
 * native调用js的bridge
 * */
interface IMagpieBridgeN2J {
    // 向js推送事件
    fun pushRequest(cmd: String, args: String)

    // 同步调用js等待返回结果
    fun syncRequest(cmd: String, args: String, callback: (String) -> Unit)

    // 异步调用js
    fun asyncRequest(cmd: String, args: String, callback: (String) -> Unit)

    fun addPushHandler(cmd: String, handler: IMagpieBridgePushHandler)

    fun registerSyncHandler(cmd: String, handler: IMagpieBridgeSyncHandler)

    fun registerAsyncHandler(cmd: String, handler: IMagpieBridgeAsyncHandler)
}