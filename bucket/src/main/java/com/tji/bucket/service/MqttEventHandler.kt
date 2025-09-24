package com.tji.bucket.service

import android.util.Log
import com.tji.bucket.data.model.LinkDevice
import com.tji.bucket.data.repository.LinkRepository
import org.json.JSONObject
import com.tji.bucket.data.model.Switch
import org.json.JSONArray

class MqttEventHandler(
    private val linkDeviceRepo: LinkRepository
) {
    suspend fun handleMessage(serialNumber: String, message: String) {
        try {
            val json = JSONObject(message)
            when (json.getString("event_type")) {
                "LinkDeviceStartup" -> handleLinkDeviceStartup(json)
                "SubDeviceAdded" -> handleSubDeviceAdded(serialNumber, json)
                "SubDeviceRemoved" -> handleSubDeviceRemoved(serialNumber, json)
                "LinkDeviceOffline" -> handleLinkDeviceOffline(serialNumber)
                "SubDeviceStatusChanged" -> handleSubDeviceStatusChanged(serialNumber, json)
            }
        } catch (e: Exception) {
            Log.e("MqttEventHandler", "解析 MQTT 消息失败: ${e.message}", e)
        }
    }

    private suspend fun handleLinkDeviceStartup(json: JSONObject) {
        val linkDevice = LinkDevice(
            event_type = json.getString("event_type"),
            serial_number = json.getString("serial_number"),
            deviceName = json.getString("deviceName"),
            deviceType = json.getString("deviceType"),
            manufacturer = json.getString("manufacturer"),
            deviceModel = json.getString("deviceModel"),
            isOnline = json.getBoolean("isOnline"),
            hwVersion = json.getString("hwVersion"),
            swVersion = json.getString("swVersion"),
            uptime = json.getInt("uptime"),
            deviceConfig = json.getString("deviceConfig"),
            subDevices = parseSubDevices(json.getJSONArray("subDevices")),
            timestamp = json.getString("timestamp")
        )
        linkDeviceRepo.updateLinkDevice(linkDevice)
    }

    private suspend fun handleSubDeviceAdded(linkSn: String, json: JSONObject) {
        val switch = Switch(
            serialNumber = json.getString("serial_number"),
            deviceName = json.getString("deviceName"),
            deviceType = json.getString("deviceType"),
            isOnline = json.getBoolean("isOnline"),
            currentAngle = json.getDouble("currentAngle"),
            currentCurrent = json.getDouble("currentCurrent"),
            inputVoltage = json.getDouble("inputVoltage"),
            servoMinAngle = json.getDouble("servoMinAngle"),
            servoMaxAngle = json.getDouble("servoMaxAngle"),
            uptime = json.getInt("uptime")
        )
        linkDeviceRepo.addSubDevice(linkSn, switch)
    }

    private suspend fun handleSubDeviceRemoved(linkSn: String, json: JSONObject) {
        val switchSn = json.getString("serial_number")
        linkDeviceRepo.removeSubDevice(linkSn, switchSn)
    }

    private suspend fun handleLinkDeviceOffline(serialNumber: String) {
        linkDeviceRepo.updateLinkDeviceStatus(serialNumber, false)
    }

    private suspend fun handleSubDeviceStatusChanged(linkSn: String, json: JSONObject) {
        val subSerialNumber = json.getString("serial_number")
        val updatedSwitch = Switch(
            serialNumber = subSerialNumber,
            deviceName = json.getString("deviceName"),
            deviceType = json.getString("deviceType"),
            isOnline = json.getBoolean("isOnline"),
            currentAngle = json.getDouble("currentAngle"),
            currentCurrent = json.getDouble("currentCurrent"),
            inputVoltage = json.getDouble("inputVoltage"),
            servoMinAngle = json.getDouble("servoMinAngle"),
            servoMaxAngle = json.getDouble("servoMaxAngle"),
            uptime = json.getInt("uptime")
        )
        linkDeviceRepo.updateSubDevice(linkSn, updatedSwitch)
    }

    private fun parseSubDevices(array: JSONArray): List<Switch> {
        return (0 until array.length()).map { i ->
            val subDevice = array.getJSONObject(i)
            Switch(
                serialNumber = subDevice.getString("serialNumber"),
                deviceName = subDevice.getString("deviceName"),
                deviceType = subDevice.getString("deviceType"),
                isOnline = subDevice.getBoolean("isOnline"),
                currentAngle = subDevice.getDouble("currentAngle"),
                currentCurrent = subDevice.getDouble("currentCurrent"),
                inputVoltage = subDevice.getDouble("inputVoltage"),
                servoMinAngle = subDevice.getDouble("servoMinAngle"),
                servoMaxAngle = subDevice.getDouble("servoMaxAngle"),
                uptime = subDevice.getInt("uptime")
            )
        }
    }
}