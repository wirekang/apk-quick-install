package com.wirekang.apkqi

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object FileManager {
    lateinit var file: File

    private lateinit var out: FileOutputStream
    private var fileSize: Long = 0

    var context: Context? = null
        set(value) {
            field = value
            file = File(field!!.filesDir, "app.apk")
            if (file.exists())
                file.delete()
        }

    fun start(size: Long) {
        this.fileSize = size
        if (file.exists())
            file.delete()
        out = FileOutputStream(file)
    }

    fun write(bytes: ByteArray) {
        out.write(bytes,0,bytes.size)
    }

    fun end() {
        out.flush()
        out.close()
    }

    private fun log(str: String) {
        Log.d("FileManager", str)
    }
}