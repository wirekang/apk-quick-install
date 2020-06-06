package com.wirekang.apkqi

import android.content.Context
import android.util.Log
import java.io.File
import java.io.RandomAccessFile

object FileManager {
    lateinit var file: File
    private lateinit var raf: RandomAccessFile

    var size: Long = 0
    var context: Context? = null
        set(value) {
            field = value
            file = File(field!!.filesDir, "app.apk")
            if (file.exists())
                file.delete()
        }


    fun start(size: Long) {
        this.size = size
        if (file.exists())
            file.delete()
        raf = RandomAccessFile(file, "rw")
    }

    fun write(offset: Long, bytes: ByteArray) {
        if (offset % 8 == 0L)
            log("${(offset / size.toDouble() * 100).toInt()}%")
        raf.seek(offset)
        raf.write(bytes)
    }

    fun end() {
        raf.close()
    }

    private fun log(str: String) {
        Log.d("FileManager", str)
    }
}