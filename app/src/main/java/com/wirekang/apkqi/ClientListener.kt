package com.wirekang.apkqi

interface ClientListener {
    fun onPortError()
    fun onHostError()
    fun onConnect()
    fun onDisconnect()
    fun onFileStart(size: Long)
    fun onFile(write: Long)
    fun onFileEnd()
}