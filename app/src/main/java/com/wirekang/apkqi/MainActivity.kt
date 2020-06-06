package com.wirekang.apkqi

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager

class MainActivity : AppCompatActivity(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var serviceIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        serviceIntent = Intent(applicationContext, MainService::class.java)

        setContentView(R.layout.activity_main)

        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        sp.registerOnSharedPreferenceChangeListener(this)
        setHost(sp)
        sp.edit().putBoolean("run", MainService.isRunning).commit()
        setFragment()
    }

    override fun onSharedPreferenceChanged(sp: SharedPreferences?, key: String?) {
        setHost(sp!!)
        when (key!!) {
            "run" -> runService(sp.getBoolean("run", false))
        }
    }

    private fun runService(run: Boolean) {
        if (run) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } else {
            MainService.isStopping = true
            stopService(serviceIntent)

        }
    }

    private fun setHost(sp: SharedPreferences) {
        Client.host = sp.getString("host", "")
        Client.port = sp.getString("port", "")!!.toInt()
    }

    private fun setFragment() {
        supportFragmentManager.beginTransaction().replace(R.id.main_ll, SettingsFragment()).commit()
    }

}