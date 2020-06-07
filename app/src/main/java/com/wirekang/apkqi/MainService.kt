package com.wirekang.apkqi

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.widget.Toast
import androidx.core.content.FileProvider

const val CHANNEL_ID = "apkquickinstall"
const val CHANNEL_NAME = "APK Quick Install"
const val MSG_START = 1
const val MSG_END = 2
const val MSG_CONNECT = 3
const val MSG_DISCONNECT = 4
const val MSG_ERROR = 5

class MainService : Service(), ClientListener {
    companion object {
        var isRunning = false
    }

    private lateinit var builder: Notification.Builder
    private lateinit var notificationManager: NotificationManager
    private lateinit var toastHandler: ToastHandler
    private var fileSize = 0L

    class ToastHandler(private val context: Context) : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_START ->
                    Toast.makeText(context, R.string.toast_start, Toast.LENGTH_LONG).show()
                MSG_END ->
                    Toast.makeText(context, R.string.toast_end, Toast.LENGTH_LONG).show()
                MSG_CONNECT ->
                    Toast.makeText(context, R.string.toast_connect, Toast.LENGTH_LONG).show()
                MSG_DISCONNECT ->
                    Toast.makeText(context, R.string.toast_disconnect, Toast.LENGTH_LONG).show()
                MSG_ERROR ->
                    Toast.makeText(context, R.string.toast_error, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        FileManager.context = applicationContext
        Client.listener = this
        createChannel(CHANNEL_ID, CHANNEL_NAME)
        builder = createBuilder()
        toastHandler = ToastHandler(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(123, builder.build())
        setContentOffline()
        Client.start()
        isRunning = true

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        Client.tryClose()
        stopSelf()
    }

    private fun createChannel(id: String, name: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                id, name, NotificationManager.IMPORTANCE_NONE
            )
            channel.enableVibration(false)
            channel.enableLights(false)
            notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createBuilder(): Notification.Builder {
        val mainIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(applicationContext, CHANNEL_ID)
        } else {
            Notification.Builder(applicationContext)
        }
        return builder.setSmallIcon(R.mipmap.ic_launcher).setContentIntent(pendingIntent)
    }

    private fun setContent(title: String, text: String) {
        builder.setContentText(text).setContentTitle(title)
        notificationManager.notify(123, builder.build())
    }

    private fun setContentWait() {
        setContent(
            resources.getString(R.string.notification_title_wait),
            resources.getString(R.string.notification_text_wait)
        )
    }

    private fun setContentError() {
        setContent(
            resources.getString(R.string.notification_title_error),
            resources.getString(R.string.notification_text_error)
        )
    }

    private fun setContentOffline() {
        setContent(
            resources.getString(R.string.notification_title_offline),
            resources.getString(R.string.notification_text_offline)
                .format("${Client.host}:${Client.port}")
        )
    }

    override fun onPortError() {
        setContentError()
        toastHandler.sendEmptyMessage(MSG_ERROR)
    }

    override fun onHostError() {
        setContentError()
        toastHandler.sendEmptyMessage(MSG_ERROR)
    }

    override fun onConnect() {
        setContentWait()
        toastHandler.sendEmptyMessage(MSG_CONNECT)
    }

    override fun onDisconnect() {
        setContentOffline()
        toastHandler.sendEmptyMessage(MSG_DISCONNECT)
    }

    override fun onFileStart(size: Long) {
        fileSize = size
        toastHandler.sendEmptyMessage(MSG_START)
    }

    override fun onFile(write: Long) {
        val percent = write / fileSize.toDouble() * 100
        val text = resources.getString(R.string.notification_text_download).format(percent.toInt())
        setContent(resources.getString(R.string.notification_title_download), text)
    }

    override fun onFileEnd() {
        toastHandler.sendEmptyMessage(MSG_END)
        openFile()
        Thread.sleep(1000)
        setContentWait()
    }

    private fun openFile() {
        val i = Intent(Intent.ACTION_VIEW)
        val uri =
            FileProvider.getUriForFile(
                applicationContext, BuildConfig.APPLICATION_ID + ".fileprovider",
                FileManager.file
            )
        i.setDataAndType(uri, "application/vnd.android.package-archive")
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(i)
    }
}