package com.tji.bucket.data.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tji.bucket.data.model.LinkDevice
import com.tji.bucket.data.model.Switch
import com.tji.bucket.data.model.SwitchControlParms
import com.tji.bucket.data.repository.LinkRepository
import com.tji.bucket.data.repository.SwitchRepo
import com.tji.bucket.data.vminterface.LinkViewModelInterface
import com.tji.bucket.data.vminterface.LoginViewModelInterface
import com.tji.bucket.data.vminterface.SwitchViewModelInterface
import com.tji.bucket.service.MqttSubscriptionManager
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

// 4. 简化后的 MainViewModel
class MainViewModel(
    private val linkViewModel: LinkViewModelInterface,
    private val switchViewModel: SwitchViewModelInterface,
    val loginViewModel: LoginViewModelInterface,
    private val mqttSubscriptionManager: MqttSubscriptionManager
) : ViewModel() {

    // 直接暴露 repository 的数据
    val links: StateFlow<List<LinkDevice>> = linkViewModel.links
    private val _linkSerialNumbers = MutableStateFlow<List<String>>(emptyList())
    val linkSerialNumbers: StateFlow<List<String>> = _linkSerialNumbers.asStateFlow()

    // 合并所有加载状态
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

    // 合并所有错误消息
    val errorMessage: StateFlow<String?> = combine(
        linkViewModel.uiState.map { it.errorMessage },
        switchViewModel.uiState.map { it.errorMessage },
        loginViewModel.uiState.map { it.errorMessage }
    ) { deviceError, switchError, loginError ->
        deviceError ?: switchError ?: loginError
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun loadLinks(userId: String) {
        viewModelScope.launch {
            try {
                Log.d("LoginViewModel", "dddddddddddddd33333333333vvvvvvvvvvvvv")

                val serialNumbers = loginViewModel.getLinksForUser(userId)
                Log.d("LoginViewModel", "dddddddddddddd344444444444vvvvvvvvvvvvv")

                _linkSerialNumbers.value = serialNumbers
                mqttSubscriptionManager.subscribeToDevices(serialNumbers)

            } catch (e: Exception) {
                Log.e("LinkViewModel", "加载链接失败: ${e.message}")
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
    }
}