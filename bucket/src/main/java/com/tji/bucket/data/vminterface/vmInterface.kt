package com.tji.bucket.data.vminterface

import androidx.lifecycle.ViewModel
import com.tji.bucket.data.model.DeviceUiState
import com.tji.bucket.data.model.LinkDevice
import com.tji.bucket.data.model.SwitchControlParms
import kotlinx.coroutines.flow.StateFlow

/**
 * 设备视图模型接口，定义设备相关状态和操作。
 * 隐藏 Repository 细节，仅暴露视图层所需方法。
 */
interface LinkViewModelInterface {
    val uiState: StateFlow<DeviceUiState>
    val isLoading: StateFlow<Boolean>
    val errorMessage: StateFlow<String?>

    /**
     * 加载指定用户ID的链接设备数据。
     * @param userId 用户ID
     */
    fun loadSwitchDevices(userId: String)

    /**
     * 刷新设备数据。
     * @param userId 用户ID
     */
    fun refreshDevices(userId: String)

    /**
     * 清除错误消息。
     */
    fun clearErrorMessage()


}

/**
 * 开关控制视图模型接口，定义开关相关状态和操作。
 * 隐藏 Repository 细节，仅暴露视图层所需方法。
 */
interface SwitchViewModelInterface {
    val uiState: StateFlow<SwitchUiState>
    val errorMessage: StateFlow<String?>

    /**
     * 设置指定设备的开关角度。
     * @param SwitchControlParms  控制参数
     */

    fun setAngle(linkSn:String ,scParms:SwitchControlParms)

    /**
     * 清除错误消息。
     */
    fun clearErrorMessage()
}

/**
 * 登录视图模型接口，定义登录相关状态和操作。
 */
interface LoginViewModelInterface {
    val uiState: StateFlow<LoginUiState>

    /**
     * 执行登录操作。
     * @param account 账号
     * @param password 密码
     * @param rememberMe 是否记住登录状态
     */
    fun login(account: String, password: String, rememberMe: Boolean,callback: (Boolean, String?) -> Unit)

    /**
     * 执行登出操作。
     */
    fun logout()

    /**
     * 清除错误消息。
     */
    fun clearErrorMessage()

    suspend fun getAllLinks(): List<String>

}


// 数据类定义
data class DeviceUiState(
    val linkDevices: List<LinkDevice> = emptyList(),
    val errorMessage: String? = null
)

data class SwitchUiState(
    val errorMessage: String? = null
)

data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userId: String? = null,
    val errorMessage: String? = null
)
