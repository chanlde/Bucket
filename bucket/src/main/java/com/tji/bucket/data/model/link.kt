package com.tji.bucket.data.model

data class LinkDevice(
    val event_type: String,
    val serial_number: String,    // 设备序列号
    val deviceName: String,       // 设备名称
    val deviceType: String,       // 设备类型
    val manufacturer: String,     // 制造商
    val deviceModel: String,      // 设备型号
    val isOnline: Boolean,        // 在线状态
    val hwVersion: String,        // 硬件版本
    val swVersion: String,        // 软件版本
//    val ipAddress: String,        // IP 地址
//    val macAddress: String,       // MAC 地址
//    val rssi: Int,                // 信号强度
    val uptime: Int,              // 运行时间（秒）
    val deviceConfig: String,     // 设备配置文件（base64 编码）
    val subDevices: List<Switch>, // 下级设备列表
    val timestamp: String         // 时间戳
)

data class DeviceUiState(
    val isLoading: Boolean = false,
    val switchDevices: List<Switch> = emptyList(),
    val errorMessage: String? = null,
    val selectedDevice: LinkDevice? = null
)