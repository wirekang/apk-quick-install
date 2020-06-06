package com.wirekang.apkqi

interface ClientListener {
    fun onConnect()
    fun onDisconnect()
    fun onFileStart(size: Long)
    fun onFile(write: Long)
    fun onFileEnd()
}