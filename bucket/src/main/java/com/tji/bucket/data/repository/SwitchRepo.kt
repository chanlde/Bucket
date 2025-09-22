package com.tji.bucket.data.repository

import android.util.Log
import com.tji.bucket.data.model.SwitchControlParms
import com.tji.network.MQTTConfig
import com.tji.network.MqttManager
import org.json.JSONObject

class SwitchRepo : SwitchRepository {

    val mqttConfig = MQTTConfig.default()

    companion object {
        private const val TAG = "FakeSwitchRepository"
    }

    // 模拟获取用户的所有 Switch 设备
    override suspend fun restart(sn: String){
    }

    // 模拟控制 Switch 设备的操作
    override suspend fun setAngle(linkSn:String ,scParms:SwitchControlParms) {

        val message = JSONObject().apply {
            put("event_type", "ServoControlRequest")
            put("serial_number", scParms.sn)
            put("angle", scParms.angle)
            put("speed", scParms.speed)
            put("mode", scParms.mode.name)
            put("timestamp", System.currentTimeMillis())

        }.toString()

        MqttManager.getInstance().publish(
            topic = mqttConfig.getControlTopic(linkSn),
            message = message,
            onSuccess = {
                Log.d("MainViewModel", "${scParms.sn},控制指令发送成功: ${mqttConfig.getControlTopic(linkSn)}, message=$message")
            },
            onError = { throwable ->
                Log.e("MainViewModel", "控制指令发送失败: ${throwable.message}")
            }
        )
    }
}
