package com.tji.bucket.data.repository

import com.dji.network.DataReportManager
import com.tji.bucket.data.model.AuthResult
import com.tji.bucket.data.model.LinkDevice
import com.tji.bucket.data.model.loginResult
import kotlinx.coroutines.delay
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * 真实的认证仓库实现，基于 DataReportManager。
 */
class AuthRepo : AuthRepository {
    override suspend fun getLinksForUser(sn: String): List<LinkDevice> {
        TODO("Not yet implemented")
    }

    override suspend fun login(account: String, password: String): loginResult = suspendCoroutine { continuation ->
        DataReportManager.getInstance().login(account, password) { success, message ->
            continuation.resume(loginResult(success, message))
        }
    }

    override suspend fun auth(userId: String): AuthResult = suspendCoroutine { continuation ->
        DataReportManager.getInstance().auth(userId) { success, message, userData ->
            continuation.resume(AuthResult(success, message, userData))
        }
    }

    override suspend fun logout() {
        // 假设 DataReportManager 提供了 logout 方法
        // 如果 logout 是回调风格的，也可以用 suspendCoroutine 包装
        // 示例：DataReportManager.getInstance().logout()
    }

    override suspend fun getAllLinks(): List<String> {
        delay(500)
        return listOf("E46424468B643D28","E46424468B294D28")
    }
}