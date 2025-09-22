package com.tji.bucket.data.repository

import android.util.Log
import com.tji.bucket.data.model.LinkDevice
import com.tji.bucket.data.model.Switch
import kotlinx.coroutines.delay

class LinkRepo : LinkRepository {

    companion object {
        private const val TAG = "FakeLinkRepository"
        private const val MOCK_SERIAL_NUMBER = "E46424468B643D28"
    }

    override suspend fun getSwitchFormLink(sn: String): List<Switch> {
        delay(800) // 模拟网络延迟
        Log.d(TAG, "获取 Link 设备 $sn 的 Switch 列表")
        return if (sn == MOCK_SERIAL_NUMBER) emptyList() else emptyList()
    }

    override suspend fun getLinkBySerialNumber(sn: String): LinkDevice? {
        delay(200) // 模拟网络延迟
        Log.d(TAG, "获取 Link 设备: $sn")
        return if (sn == MOCK_SERIAL_NUMBER) {
            LinkDevice(
                event_type = "device_status",
                serial_number = MOCK_SERIAL_NUMBER,
                deviceName = "",
                deviceType = "",
                manufacturer = "",
                deviceModel = "",
                isOnline = false,
                hwVersion = "",
                swVersion = "",
                uptime = 0,
                deviceConfig = "",
                subDevices = emptyList(),
                timestamp = System.currentTimeMillis().toString()
            )
        } else null
    }

    override suspend fun refreshLinkStatus(sn: String): LinkDevice? {
        delay(500) // 模拟网络延迟
        Log.d(TAG, "刷新 Link 设备状态: $sn")
        return if (sn == MOCK_SERIAL_NUMBER) {
            LinkDevice(
                event_type = "device_status",
                serial_number = MOCK_SERIAL_NUMBER,
                deviceName = "",
                deviceType = "",
                manufacturer = "",
                deviceModel = "",
                isOnline = false,
                hwVersion = "",
                swVersion = "",

                uptime = 0,
                deviceConfig = "",
                subDevices = emptyList(),
                timestamp = System.currentTimeMillis().toString()
            )
        } else null
    }

}