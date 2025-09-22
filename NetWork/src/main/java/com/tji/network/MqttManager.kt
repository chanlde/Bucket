package com.tji.network

import android.util.Log
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttClientState
import com.hivemq.client.mqtt.MqttGlobalPublishFilter
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import java.util.concurrent.TimeUnit
import kotlin.also
import kotlin.run
import kotlin.text.decodeToString
import kotlin.text.toByteArray

class MqttManager private constructor(private val config: MQTTConfig) {

    companion object {
        @Volatile
        private var INSTANCE: MqttManager? = null

        fun getInstance(config: MQTTConfig? = null): MqttManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: run {
                    val finalConfig = config ?: MQTTConfig.default()
                    MqttManager(finalConfig).also { INSTANCE = it }
                }
            }
        }

        // 提供一个便捷方法，使用默认配置
        fun getInstance(): MqttManager {
            return getInstance(null)
        }
    }

    private val client: Mqtt3AsyncClient = MqttClient.builder()
        .useMqttVersion3()
        .identifier(config.clientId)
        .serverHost(config.serverHost)
        .serverPort(config.serverPort)


        // 重连设置
        .automaticReconnect()
        .initialDelay(1, TimeUnit.SECONDS) // 断线1秒后开始自动重连，如果重连还失败，则下次会等时间会按指数增长，比如2秒、4秒、8秒，双倍增长等待时间，但是不会超过最大值，由maxDelay函数来指定最大值。
        .maxDelay(32, TimeUnit.SECONDS)    // 断线后最多32秒就会自动重连，第5次连会来到32的位置，前面4次已用掉31秒的等待时间了。
        .applyAutomaticReconnect()

        // 连接状态监听器设置
        .addConnectedListener {
            Log.d("vvvvvv","MQTT${it.clientConfig.serverHost}:${it.clientConfig.serverPort}连接成功")
        }
        .addDisconnectedListener {
            // 客户端断开连接，或者连接失败都会回调这里
             Log.d("vvvvvv","MQTT${it.clientConfig.serverHost}:${it.clientConfig.serverPort}连接断开：${it.cause.message}，连接状态：${it.clientConfig.state.name}")
            when (it.clientConfig.state) {
                MqttClientState.CONNECTING ->  Log.d("vvvvvv","手动连接失败")             // 即主动调用connect时没连接成功
                MqttClientState.CONNECTING_RECONNECT ->  Log.d("vvvvvv","自动重连失败")   // 即连接成功后异常断开自动重连时连接失败
                MqttClientState.CONNECTED ->  Log.d("vvvvvv","连接正常断开或异常断开")
                else ->  Log.d("vvvvvv","连接断开：${it.clientConfig.state.name}")
            }
        }
        .buildAsync()

        .also {
            it.publishes(MqttGlobalPublishFilter.ALL) { publish: Mqtt3Publish ->
                 Log.d("vvvvvv","收到${publish.topic}的消息：${String(publish.payloadAsBytes)}")
            }
        }

    private var isConnected = false

    // 定义一个消息回调变量，外部可设置
    var onMessageReceived: ((String) -> Unit)? = null

    init {
        //connect()
    }

    fun connect(
        onConnected: (() -> Unit)? = null,
        onFailed: ((Throwable) -> Unit)? = null
    ) {
         Log.d("vvvvvv","MQTT connect called")

        client.connectWith()
            .simpleAuth()
            .username(config.username)
            .password(config.password.toByteArray())
            .applySimpleAuth()
            .send()
            .whenComplete { _, throwable ->
                if (throwable != null) {
                     Log.d("vvvvvv","MQTT connect failed: ${throwable.message}")
                    onFailed?.invoke(throwable)
                } else {
                     Log.d("vvvvvv","MQTT connected")
                    isConnected = true
                    onConnected?.invoke()

                }
            }
            .exceptionally { ex ->
                 Log.d("vvvvvv","MQTT connection exceptionally failed: ${ex.message}")
                null
            }

         Log.d("vvvvvv","MQTT connect call sent")
    }

    fun subscribe(
        topic: String,
        onMessage: (String) -> Unit,
        onError: ((Throwable) -> Unit)? = null
    ) {
        if (!isConnected) {
             Log.d("vvvvvv","MQTT not connected, cannot subscribe.")
            onError?.invoke(kotlin.Exception("MQTT not connected"))
            return
        }

        client.toAsync().subscribeWith()
            .topicFilter(topic)
            .callback { publish ->
                val message = publish.payloadAsBytes.decodeToString()
                onMessage(message)
            }
            .send()
            .whenComplete { _, throwable ->
                if (throwable != null) {
                     Log.d("vvvvvv","MQTT subscribe failed: ${throwable.message}")
                    onError?.invoke(throwable)
                } else {
                     Log.d("vvvvvv","MQTT subscribed to $topic")
                }
            }
    }

    fun publish(
        topic: String,
        message: String,
        onSuccess: (() -> Unit)? = null,
        onError: ((Throwable) -> Unit)? = null
    ) {
        if (!isConnected) {
             Log.d("vvvvvv","MQTT not connected, cannot publish.")
            onError?.invoke(kotlin.Exception("MQTT not connected"))
            return
        }

        client.publishWith()
            .topic(topic)
            .payload(message.toByteArray(Charsets.UTF_8))
            .send()
            .whenComplete { _, throwable ->
                if (throwable != null) {
                     Log.d("vvvvvv","MQTT publish failed: ${throwable.message}")
                    onError?.invoke(throwable)
                } else {
                     Log.d("vvvvvv","MQTT publish succeeded")
                    onSuccess?.invoke()
                }
            }
    }

    fun disconnect(
        onSuccess: (() -> Unit)? = null,
        onError: ((Throwable) -> Unit)? = null
    ) {
        if (!isConnected) {
             Log.d("vvvvvv","MQTT not connected, cannot disconnect.")
            onError?.invoke(kotlin.Exception("MQTT not connected"))
            return
        }

        client.disconnect()
            .whenComplete { _, throwable ->
                if (throwable != null) {
                     Log.d("vvvvvv","MQTT disconnect failed: ${throwable.message}")
                    onError?.invoke(throwable)
                } else {
                     Log.d("vvvvvv","MQTT disconnected")
                    isConnected = false
                    onSuccess?.invoke()
                }
            }
    }

    // 获取连接状态
    fun isConnected(): Boolean = isConnected

    // 获取当前配置
    fun getConfig(): MQTTConfig = config
}