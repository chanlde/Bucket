package com.tji.bucket.data.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tji.bucket.data.model.LinkDevice
import com.tji.bucket.data.model.Switch
import com.tji.bucket.data.model.SwitchControlParms
import com.tji.bucket.data.repository.SwitchRepo
import com.tji.bucket.data.vminterface.LinkViewModelInterface
import com.tji.bucket.data.vminterface.LoginViewModelInterface
import com.tji.bucket.data.vminterface.SwitchViewModelInterface
import com.tji.network.MQTTConfig
import com.tji.network.MqttManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * 主视图模型，负责协调设备、开关和登录相关的状态和操作。
 * 通过接口解耦具体实现，增强可测试性和扩展性。
 */
class MainViewModel(
    private val linkViewModel: LinkViewModelInterface,
    private val switchViewModel: SwitchViewModelInterface,
    val loginViewModel: LoginViewModelInterface,
) : ViewModel() {

    private val subscribedTopics = mutableSetOf<String>()

    private val _links = MutableStateFlow<List<LinkDevice>>(emptyList())
    val links: StateFlow<List<LinkDevice>> = _links.asStateFlow()

    private val _linkSerialNumbers = MutableStateFlow<List<String>>(emptyList())
    val linkSerialNumbers: StateFlow<List<String>> = _linkSerialNumbers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> = combine(
        linkViewModel.isLoading,
        loginViewModel.uiState.map { it.isLoading }
    ) { deviceLoading, loginLoading ->
        deviceLoading || loginLoading
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    private val _combinedErrorMessage = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            combine(
                linkViewModel.uiState.map { it.errorMessage },
                switchViewModel.uiState.map { it.errorMessage },
                loginViewModel.uiState.map { it.errorMessage }
            ) { deviceError, switchError, loginError ->
                deviceError ?: switchError ?: loginError
            }.collect { errorMessage ->
                _combinedErrorMessage.value = errorMessage
            }
        }

        viewModelScope.launch {
            linkViewModel.uiState.collect { deviceState ->
                val switches = deviceState.switchDevices
                Log.d("MainViewModel", "Switches from linkViewModel: $switches")
            }
        }
    }

    fun loadLinks(userId: String) {
        viewModelScope.launch {
            Log.d("MainViewModel", "Loading links for userId: $userId")
            _isLoading.value = true
            try {
                _linkSerialNumbers.value = loginViewModel.getAllLinks()
                subscribeToTopics() // 加载后重新订阅

            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSwitchAngle(linkSn: String, scParms: SwitchControlParms) {
        switchViewModel.setAngle(linkSn, scParms)
    }

    fun refreshDevices(userId: String) {
        linkViewModel.refreshDevices(userId)
    }

    fun login(account: String, password: String, rememberMe: Boolean, callback: (Boolean, String?) -> Unit) {
        loginViewModel.login(account, password, rememberMe, callback)
    }

    fun logout() {
        loginViewModel.logout()
    }

    fun clearErrorMessage() {
        linkViewModel.clearErrorMessage()
        switchViewModel.clearErrorMessage()
        loginViewModel.clearErrorMessage()
        _combinedErrorMessage.value = null
    }

    fun handleMqttMessage(serialNumber: String, message: String) {
        Log.d("MainViewModel", "收到 MQTT 消息，serialNumber: $serialNumber, 消息内容: $message")
        try {
            val json = JSONObject(message)
            Log.d("MainViewModel", "解析 JSON 成功: ${json.toString(2)}")

            when (val eventType = json.getString("event_type")) {
                "LinkDeviceStartup" -> {
                    Log.d("MainViewModel", "处理 LinkDeviceStartup 事件，serialNumber: $serialNumber")
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
                        subDevices = json.getJSONArray("subDevices").let { array ->
                            Log.d("MainViewModel", "解析 subDevices 数组，长度: ${array.length()}")
                            (0 until array.length()).map { i ->
                                val subDevice = array.getJSONObject(i)
                                Log.d("MainViewModel", "解析子设备: ${subDevice.toString(2)}")
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
                        },
                        timestamp = json.getString("timestamp")
                    )
                    Log.d("MainViewModel", "创建 LinkDevice: ${linkDevice.serial_number}, 包含 ${linkDevice.subDevices.size} 个子设备")

                    _links.update { current ->
                        val updatedList = current.toMutableList()
                        val existingIndex = current.indexOfFirst { it.serial_number == linkDevice.serial_number }
                        if (existingIndex >= 0) {
                            Log.d("MainViewModel", "更新现有 LinkDevice: ${linkDevice.serial_number}")
                            updatedList[existingIndex] = linkDevice
                        } else {
                            Log.d("MainViewModel", "添加新 LinkDevice: ${linkDevice.serial_number}")
                            updatedList.add(linkDevice)
                        }
                        Log.d("MainViewModel", "更新后 _links 包含 ${updatedList.size} 个设备")
                        updatedList
                    }

                    _linkSerialNumbers.update { current ->
                        val updatedList = current.toMutableList()
                        if (!updatedList.contains(linkDevice.serial_number)) {
                            Log.d("MainViewModel", "添加新 serialNumber: ${linkDevice.serial_number} 到 _linkSerialNumbers")
                            updatedList.add(linkDevice.serial_number)
                            subscribeToNewLink(linkDevice.serial_number)
                        }
                        Log.d("MainViewModel", "更新后 _linkSerialNumbers 包含 ${updatedList.size} 个序列号")
                        updatedList
                    }
                }

                "SubDeviceAdded" -> {
                    Log.d("MainViewModel", "cccccccccccccccccccccccc$serialNumber")
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
                    addSwitch(serialNumber,switch)
                }

                "SubDeviceRemoved" -> {
                    Log.d("MainViewModel", "dddddddddddddddddd")
                    val switchSn = json.getString("serial_number")
                    removeSwitch(serialNumber,switchSn)
                }

                "LinkDeviceOffline" -> {
                    Log.d("MainViewModel", "处理 LinkDeviceOffline 事件，serialNumber: $serialNumber")
                    _links.update { current ->
                        current.map { link ->
                            if (link.serial_number == serialNumber) {
                                Log.d("MainViewModel", "设置 LinkDevice ${link.serial_number} 为离线")
                                link.copy(isOnline = false)
                            } else {
                                link
                            }
                        }
                    }
                }

                "SubDeviceStatusChanged" -> {
                    Log.d("MainViewModel", "cccccccccccccccccc处理 SubDeviceStatusChanged 事件，serialNumber: $serialNumber")
                    val subSerialNumber = json.getString("serial_number")
                    val deviceName = json.getString("deviceName")
                    val deviceType = json.getString("deviceType")
                    val isOnline = json.getBoolean("isOnline")
                    val currentAngle = json.getDouble("currentAngle")
                    val currentCurrent = json.getDouble("currentCurrent")
                    val inputVoltage = json.getDouble("inputVoltage")
                    val servoMinAngle = json.getDouble("servoMinAngle")
                    val servoMaxAngle = json.getDouble("servoMaxAngle")
                    val uptime = json.getInt("uptime")

                    _links.update { current ->
                        current.map { link ->
                            if (link.serial_number == serialNumber) {
                                link.copy(
                                    subDevices = link.subDevices.map { switch ->
                                        if (switch.serialNumber == subSerialNumber) {
                                            Log.d("MainViewModel", "更新 Switch: ${switch.serialNumber} 的状态")
                                            switch.copy(
                                                deviceName = deviceName,
                                                deviceType = deviceType,
                                                isOnline = isOnline,
                                                currentAngle = currentAngle,
                                                currentCurrent = currentCurrent,
                                                inputVoltage = inputVoltage,
                                                servoMinAngle = servoMinAngle,
                                                servoMaxAngle = servoMaxAngle,
                                                uptime = uptime
                                            )
                                        } else {
                                            switch
                                        }
                                    }
                                )
                            } else {
                                link
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainViewModel", "解析 MQTT 消息失败: ${e.message}", e)
        }
    }

    private fun subscribeToNewLink(serialNumber: String) {
        viewModelScope.launch {
            val topic = MQTTConfig.default().getStatusTopic(serialNumber)
            Log.d("MainViewModel", "尝试订阅新设备主题: $topic, serialNumber: $serialNumber")
            if (!subscribedTopics.contains(topic)) {
                MqttManager.getInstance().subscribe(
                    topic = topic,
                    onMessage = { message ->
                        Log.d("MainViewModel", "收到主题 $topic 的消息，serialNumber: $serialNumber")
                        handleMqttMessage(serialNumber, message)
                    },
                    onError = { throwable ->
                        Log.e("MainViewModel", "订阅失败: $topic, 错误: ${throwable.message}")
                    }
                )
                subscribedTopics.add(topic)
                Log.d("MainViewModel", "成功订阅主题: $topic")
            } else {
                Log.d("MainViewModel", "主题 $topic 已订阅，跳过")
            }
        }
    }

    private fun addSwitch(linkSn: String, newSwitch: Switch) {
        viewModelScope.launch {
            // 打印添加设备的信息
            Log.d("MainViewModel", "Adding new Switch with SN: ${newSwitch.serialNumber} to LinkDevice with SN: $linkSn")

            // 更新 links 列表，向指定 LinkDevice 下添加新的 Switch
            val updatedLinks = _links.value.map { linkDevice ->
                // 判断 LinkDevice 是否是我们要操作的设备
                if (linkDevice.serial_number == linkSn) {
                    // 将新的 Switch 添加到该 LinkDevice 的 subDevices 列表中
                    val updatedSubDevices = linkDevice.subDevices + newSwitch

                    // 打印添加的 Switch 信息
                    Log.d("MainViewModel", "Added new Switch: ${newSwitch.serialNumber} to LinkDevice: $linkSn")

                    // 返回更新后的 LinkDevice，添加新的 Switch
                    linkDevice.copy(subDevices = updatedSubDevices)
                } else {
                    // 如果不是目标 LinkDevice，则保持原样
                    linkDevice
                }
            }

            // 更新 _links 状态，保存修改后的设备列表
            _links.value = updatedLinks
            Log.d("MainViewModel", "Updated LinkDevices: ${_links.value}")
        }
    }


    private fun removeSwitch(linkSn: String, switchSn: String) {
        viewModelScope.launch {
            // 打印移除设备的信息
            Log.d("MainViewModel", "Removing Switch with SN: $switchSn from LinkDevice with SN: $linkSn")

            // 更新 links 列表，移除指定 LinkDevice 下的 Switch
            val updatedLinks = _links.value.map { linkDevice ->
                // 判断 LinkDevice 是否是我们要操作的设备
                if (linkDevice.serial_number == linkSn) {
                    // 过滤掉指定的 Switch (通过 switchSn)
                    val updatedSubDevices = linkDevice.subDevices.filterNot { it.serialNumber == switchSn }

                    // 打印已移除的 Switch 信息
                    if (linkDevice.subDevices.size != updatedSubDevices.size) {
                        Log.d("MainViewModel", "Switch removed: $switchSn from LinkDevice: $linkSn")
                    } else {
                        Log.d("MainViewModel", "No Switch found with SN: $switchSn in LinkDevice: $linkSn")
                    }

                    // 返回更新后的 LinkDevice，不删除 LinkDevice，只删除 SubDevices 中的 Switch
                    linkDevice.copy(subDevices = updatedSubDevices)
                } else {
                    // 如果不是目标 LinkDevice，则保持原样
                    linkDevice
                }
            }

            // 更新 _links 状态，保存修改后的设备列表
            _links.value = updatedLinks
            Log.d("MainViewModel", "Updated LinkDevices: ${_links.value}")
        }
    }


    private fun subscribeToTopics() {
        viewModelScope.launch {
            Log.d("MainViewModel", "开始订阅所有设备主题，_linkSerialNumbers: ${_linkSerialNumbers.value}")
            if (_linkSerialNumbers.value.isEmpty()) {
                Log.d("MainViewModel", "无设备序列号可订阅")
                return@launch
            }
            _linkSerialNumbers.value.map { serialNumber ->
                async {
                    listOf(
                        MQTTConfig.default().getStatusTopic(serialNumber),
                        MQTTConfig.default().getLifecycleTopic(serialNumber)
                    ).forEach { topic ->
                        Log.d("MainViewModel", "检查主题: $topic, serialNumber: $serialNumber")
                        if (!subscribedTopics.contains(topic)) {
                            Log.d("MainViewModel", "订阅主题: $topic")
                            MqttManager.getInstance().subscribe(
                                topic = topic,
                                onMessage = { message ->
                                    Log.d("MainViewModel", "收到主题 $topic 的消息，serialNumber: $serialNumber")
                                    handleMqttMessage(serialNumber, message)
                                },
                                onError = { throwable ->
                                    Log.e("MainViewModel", "订阅失败: $topic, 错误: ${throwable.message}")
                                }
                            )
                            subscribedTopics.add(topic)
                            Log.d("MainViewModel", "成功订阅主题: $topic")
                        } else {
                            Log.d("MainViewModel", "主题 $topic 已订阅，跳过")
                        }
                    }
                }
            }.awaitAll()
            Log.d("MainViewModel", "所有主题订阅完成")
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}