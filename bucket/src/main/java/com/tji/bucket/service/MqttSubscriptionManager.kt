package com.tji.bucket.service

import android.util.Log
import com.tji.network.MQTTConfig
import com.tji.network.MqttManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.async

class MqttSubscriptionManager(
    private val mqttEventHandler: MqttEventHandler
) {
    private val subscribedTopics = mutableSetOf<String>()

    suspend fun subscribeToDevices(serialNumbers: List<String>) = coroutineScope {
        serialNumbers.map { serialNumber ->
            async {
                subscribeToDevice(serialNumber)
            }
        }.awaitAll()
    }

    private suspend fun subscribeToDevice(serialNumber: String) {
        val topics = listOf(
            MQTTConfig.default().getStatusTopic(serialNumber),
            MQTTConfig.default().getLifecycleTopic(serialNumber)
        )

        topics.forEach { topic ->
            if (!subscribedTopics.contains(topic)) {
                MqttManager.getInstance().subscribe(
                    topic = topic,
                    onMessage = { message ->
                        CoroutineScope(Dispatchers.IO).launch {
                            mqttEventHandler.handleMessage(serialNumber, message)
                        }
                    },
                    onError = { throwable ->
                        Log.e("MqttSubscriptionManager", "订阅失败: $topic, 错误: ${throwable.message}")
                    }
                )
                subscribedTopics.add(topic)
                Log.d("MqttSubscriptionManager", "成功订阅主题: $topic")
            }
        }
    }

    fun subscribeToNewDevice(serialNumber: String) {
        CoroutineScope(Dispatchers.IO).launch {
            subscribeToDevice(serialNumber)
        }
    }
}