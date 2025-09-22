package com.tji.bucket.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.provider.Settings
import com.tji.bucket.util.userData
import com.tji.bucket.util.userData.updateMqttConfig
import com.tji.network.MqttManager

class MqttService : Service() {

    override fun onCreate() {
        super.onCreate()
        val androidId = Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID)

        updateMqttConfig(clientId = androidId)

        val mqttManager = MqttManager.getInstance(userData.mqttConfig)

        mqttManager.connect(
            onConnected = {
            },
            onFailed = { throwable ->
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        MqttManager.getInstance().disconnect()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // 不需要绑定服务
    }
}