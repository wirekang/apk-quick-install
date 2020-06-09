package com.wirekang.apkqi

import android.util.Log
import kotlinx.coroutines.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ConnectException
import java.net.Socket
import java.net.SocketException
import java.net.UnknownHostException
import java.nio.charset.Charset

object Client {
    var host: String? = ""
    var port: Int = 0
    var listener: ClientListener? = null

    private lateinit var socket: Socket
    private lateinit var input: DataInputStream
    private lateinit var output: DataOutputStream
    private lateinit var job: Job
    private var fileSize = 0L
    private var wroteByte = 0L
    private var isConnected = false

    fun start() {
        job = GlobalScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    connect()
                } catch (e: UnknownHostException) {
                    listener?.onHostError()
                    tryClose()
                } catch (e: IllegalArgumentException) {
                    listener?.onPortError()
                    tryClose()
                } catch (e: SocketException) {
                    tryClose()
                } catch (e: ConnectException) {
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                onDisconnect()
                Thread.sleep(3000)
            }
        }
    }

    private fun connect() {
        log("connecting to $host:$port")
        socket = Socket(host, port)
        onConnect()

        input = DataInputStream(socket.getInputStream())
        output = DataOutputStream(socket.getOutputStream())
        listen()
    }

    private fun listen() {
        while (isConnected) {
            val length = input.readShort().toInt()
            val byteArray = ByteArray(length)
            input.read(byteArray, 0, length)
            onEvent(byteArray.toString(Charset.forName("utf-8")).trim())
        }
    }

    private fun onEvent(event: String) {
        log("event: $event")
        when (event) {
            "file start" -> {
                onFileStart(input.readLong())
            }
            "file" -> {
                val bytes = ByteArray(DEFAULT_BUFFER_SIZE)
                var read: Int
                do {
                    read = input.read(bytes, 0, DEFAULT_BUFFER_SIZE)
                    onFile(bytes.sliceArray(0 until read))
                } while (read > 0)
            }
        }
    }

    private fun onFileStart(size:Long) {
        fileSize=size
        wroteByte=0
        log("size: $fileSize")
        FileManager.start(fileSize)
        listener?.onFileStart(fileSize)
    }

    private fun onFile(bytes: ByteArray) {
        wroteByte += bytes.size
        log("write ${bytes.size}bytes total:$wroteByte / $fileSize")
        FileManager.write(bytes)
        listener?.onFile(wroteByte)
        if (wroteByte == fileSize) {
            wroteByte=0
            FileManager.end()
            listener?.onFileEnd()
            socket.close()
            connect()
        }
    }

    private fun onDisconnect() {
        log("disconnected")
        isConnected = false
        listener?.onDisconnect()
    }

    private fun onConnect() {
        log("connected")
        isConnected = true
        listener?.onConnect()
    }

    fun tryClose() {
        log("tryClose")
        try {
            job.cancel()
            socket.close()
        } catch (e: Exception) {
        }
    }

    private fun log(str: String) {
        Log.d("Client", str)
    }


}