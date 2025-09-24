package com.tji.network

data class MQTTConfig(
    val serverHost: String,
    val serverPort: Int,
    val clientId: String,
    val username: String,
    val password: String,
    val subscribeTopic: String,
    val publishTopicPrefix: String,
    val enableTLS: Boolean = true,
    val keepAliveInterval: Int = 60,
    val cleanSession: Boolean = true,
    val qos: Int = 1
) {
    companion object {
        fun default(
        ) = MQTTConfig(
            serverHost = "146.56.250.203",
            serverPort = 1883,
            clientId = "",
            username = "",
            password = "",
            subscribeTopic = "FireBucket/devices/",
            publishTopicPrefix = "FireBucket/devices/",
            enableTLS = false,
            keepAliveInterval = 60,
            cleanSession = true,
            qos = 1
        )
    }

    fun getControlTopic(serialNumber: String): String {
        return "$publishTopicPrefix$serialNumber/control"
    }

    fun getStatusTopic(serialNumber: String): String {
        return "$subscribeTopic$serialNumber/status"
    }

    fun getLifecycleTopic(serialNumber: String): String {
        return "$subscribeTopic$serialNumber/lifecycle"
    }

}