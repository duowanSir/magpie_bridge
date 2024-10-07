package com.lf.magpie_bridge

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lf.magpie_bridge_js.BridgeWebContainer

class SharedVM : ViewModel() {
    val message = MutableLiveData<Triple<String, String, String>>()

    val loopBack = MutableLiveData<String>()

    fun appendLog(log: String) {
        var previousVal = loopBack.value
        if (previousVal.isNullOrBlank()) {
            previousVal = ""
        } else {
            previousVal += "\n"
        }
        loopBack.postValue("$previousVal$log")
    }
}