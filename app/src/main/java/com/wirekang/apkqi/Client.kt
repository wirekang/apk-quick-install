package com.wirekang.apkqi

import android.util.Log

object Client {
    var host: String? = ""
    var port: String? = ""
    var listener: ClientListener? = null

    private var isConnected = false
    private var fileSize = 0L

    fun connect() {
        log("connecting to http://$host:$port")

    }

    fun tryClose() {
        log("tryClose")
        try {
        } catch (e: Exception) {
        }
    }

    private fun log(str: String) {
        Log.d("Client", str)
    }
}