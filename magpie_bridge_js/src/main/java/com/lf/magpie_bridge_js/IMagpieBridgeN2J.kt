package com.lf.magpie_bridge_js

/**
 * native调用js的bridge
 * */
interface IMagpieBridgeN2J {
    // 向js推送事件
    fun pushEvent(eventType: String, args: String)

    // 同步调用js等待返回结果
    fun syncRequest(funcName: String, args: String): String?

    // 异步调用js
    fun asyncRequest(funcName: String, args: String, callback: (String) -> Unit)
}