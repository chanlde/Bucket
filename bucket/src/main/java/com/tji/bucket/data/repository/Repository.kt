package com.tji.bucket.data.repository

import com.tji.bucket.data.model.AuthResult
import com.tji.bucket.data.model.LinkDevice
import com.tji.bucket.data.model.Switch
import com.tji.bucket.data.model.SwitchControlParms
import com.tji.bucket.data.model.getLinksResult
import com.tji.bucket.data.model.loginResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 设备数据仓库接口，定义设备相关的数据访问方法。
 */
interface LinkRepository {

    val links: StateFlow<List<LinkDevice>>


    suspend fun updateLinkDevice(linkDevice: LinkDevice)

    suspend fun addSubDevice(linkSn: String, switch: Switch)

    suspend fun removeSubDevice(linkSn: String, switchSn: String)

    suspend fun updateLinkDeviceStatus(serialNumber: String, isOnline: Boolean)

    suspend fun updateSubDevice(linkSn: String, updatedSwitch: Switch)


}

/**
 * 开关数据仓库接口，定义开关相关的数据访问和控制方法。
 */
interface SwitchRepository {
    suspend fun restart(sn: String)
    suspend fun setAngle(linkSn:String,scParms:SwitchControlParms)
}

/**
 * 认证数据仓库接口，定义登录、认证和登出相关的方法。
 */
interface AuthRepository {
    // 获取用户关联的设备列表

    // 执行登录操作，返回登录结果
    suspend fun login(account: String, password: String): loginResult

    // 执行认证操作，返回认证结果
    suspend fun auth(userId: String): AuthResult

    suspend fun getLinksForUser(userId: String): getLinksResult

    // 执行登出操作
    suspend fun logout()
}

