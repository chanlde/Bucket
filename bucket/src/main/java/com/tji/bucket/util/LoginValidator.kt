package com.tji.bucket.util

import android.content.Context
import com.tji.network.MQTTConfig

// 验证工具
object userData {
    // 存储 MQTT 配置信息，默认为 MQTTConfig.default()
    var mqttConfig: MQTTConfig = MQTTConfig.default()

    // 用于存储用户 ID，注意此字段是私有的且需要在调用前初始化
    private lateinit var userId: String

    /**
     * 更新 MQTT 配置信息。
     * 如果传入的 username 或 clientId 为 null，则保持原有值不变。
     * @param username 用于更新 MQTT 配置中的用户名
     * @param clientId 用于更新 MQTT 配置中的客户端 ID
     */
    fun updateMqttConfig(username: String? = null, clientId: String? = null) {
        // 更新 mqttConfig，保持非 null 参数值，null 参数则使用现有值
        mqttConfig = mqttConfig.copy(
            username = username ?: mqttConfig.username,  // 如果 username 为 null，使用现有值
            clientId = clientId ?: mqttConfig.clientId   // 如果 clientId 为 null，使用现有值
        )
    }

    /**
     * 设置用户 ID。
     * @param userId 用户 ID
     */
    fun setUserId(userId: String) {
        // 设置 userId，确保在调用之前初始化该变量
        this.userId = userId
    }

    /**
     * 获取当前的用户 ID。
     * @return 返回已设置的用户 ID
     */
    fun getUserId(): String {
        return userId
    }
}
