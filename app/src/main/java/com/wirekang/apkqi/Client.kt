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

    lateinit var socket: Socket
    private var fileSize = 0L
    private var isConnected = false
    private lateinit var input: DataInputStream
    private lateinit var output: DataOutputStream
    private lateinit var job: Job

    fun start() {
        job = GlobalScope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    connect()
                } catch (e: UnknownHostException) {
                    listener?.onHostError()
                } catch (e: IllegalArgumentException) {
                    listener?.onPortError()
                } catch (e: SocketException) {
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
            val length = input.readUnsignedShort()
            val byteArray = ByteArray(length)
            input.read(byteArray, 0, length)
            onEvent(byteArray.toString(Charset.forName("utf-8")).trim())
        }
    }

    private fun onEvent(e: String) {
        val event =
            if (e.length > 20) {
                e.substring(0, 20)
            } else {
                e
            }
        log("event: $event")
        when (event) {
            "file start" -> {
                fileSize = input.readLong()
                onFileStart()
            }
            "file" -> {
                val offset = input.readLong()
                val size = input.readShort().toInt()
                val bytes = ByteArray(size)
                input.read(bytes, 0, size)
                onFile(offset, bytes)
            }
        }
    }

    private fun onFileStart() {
        log("size: $fileSize")
        FileManager.start(fileSize)
        listener?.onFileStart(fileSize)
    }

    private fun onFile(offset: Long, bytes: ByteArray) {
        log("offset: $offset size: ${bytes.size}")
        FileManager.write(offset, bytes)
        listener?.onFile(offset + bytes.size)
        if (offset + bytes.size == fileSize) {
            FileManager.end()
            sendEnd()
            listener?.onFileEnd()
        }
    }

    private fun sendEnd() {
        val event = "file end"
        output.writeShort(event.length)
        output.write(event.toByteArray(Charset.forName("utf-8")))
        output.writeLong(fileSize)
        output.flush()
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