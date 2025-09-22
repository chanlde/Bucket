package com.tji.bucket.util

import android.content.Context
import com.tji.network.MQTTConfig

// 验证工具
object userData {
    var mqttConfig: MQTTConfig = MQTTConfig.default()

    private lateinit var userId: String

    // 接收完整的 MQTTConfig 对象
    fun updateMqttConfig(username: String? = null, clientId: String? = null) {
        mqttConfig = mqttConfig.copy(
            username = username ?: mqttConfig.username,
            clientId = clientId ?: mqttConfig.clientId
        )
    }

    fun setUserId(userId: String) {
        this.userId = userId
    }

    fun getUserId() : String{
        return userId
    }
}