package com.dji.network

import android.os.Build
import android.util.Log
import com.dji.network.utils.NetWorkUtils.Companion.port1
import com.dji.network.utils.NetWorkUtils.Companion.port2
import com.dji.network.utils.NetWorkUtils.Companion.serverIp
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class DataReportManager private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: DataReportManager? = null
        fun getInstance(): DataReportManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DataReportManager().also { INSTANCE = it }
            }
        }
        private const val TAG = "DataReportManager"
        private const val BASE_URL = "http://${serverIp}:${port1}"
    }

    private var client: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(15, TimeUnit.SECONDS)
        .build()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var authToken: String? = null
    var userId: String? = null

    /** ====================== 通用API结果封装 ====================== */
    sealed class ApiResult<out T> {
        data class Success<T>(val data: T, val message: String = "操作成功") : ApiResult<T>()
        data class Error(val message: String, val code: Int = -1) : ApiResult<Nothing>()
    }

    /** ====================== 通用HTTP请求方法 ====================== */
    private suspend fun executeRequest(
        request: Request,
        needsAuth: Boolean = false
    ): ApiResult<JSONObject> {
        return withContext(Dispatchers.IO) {
            try {
                // 添加认证头
                val finalRequest = if (needsAuth && !authToken.isNullOrEmpty()) {
                    request.newBuilder().addHeader("token", authToken!!).build()
                } else {
                    request
                }

                Log.d(TAG, "请求 URL: ${finalRequest.url}")

                client.newCall(finalRequest).execute().use { response ->
                    val bodyStr = response.body?.string()
                    Log.d(TAG, "HTTP 状态码: ${response.code}")
                    Log.d(TAG, "响应内容: $bodyStr")

                    when {
                        !response.isSuccessful -> {
                            val errorMsg = when (response.code) {
                                401 -> "登录已过期，请重新登录"
                                403 -> "权限不足"
                                404 -> "请求的资源不存在"
                                500 -> "服务器错误，请稍后重试"
                                else -> "请求失败(${response.code})"
                            }
                            ApiResult.Error(errorMsg, response.code)
                        }
                        bodyStr.isNullOrBlank() -> {
                            // 空 body 也当成功，但 data 是空 JSON
                            ApiResult.Success(JSONObject())
                        }

                        else -> {
                            parseJsonResponse(bodyStr.toString())
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "请求异常: ${request.url}", e)
                val errorMsg = when (e) {
                    is java.net.UnknownHostException -> "网络连接失败，请检查网络"
                    is java.net.SocketTimeoutException -> "请求超时，请重试"
                    is javax.net.ssl.SSLException -> "安全连接失败"
                    else -> "网络异常"
                }
                ApiResult.Error(errorMsg)
            }
        }
    }

    private fun parseJsonResponse(bodyStr: String): ApiResult<JSONObject> {
        return try {
            val json = JSONObject(bodyStr)
            val code = json.optInt("code", -1)
            val message = json.optString("message", "未知错误")

            Log.d("$TAG","111111111111111111111111111$code")

            when (code) {
                200 -> ApiResult.Success(json, "操作成功")
                401 -> {
                    // token过期，清除本地认证信息
                    clearAuthInfo()
                    ApiResult.Error("登录已过期，请重新登录", code)
                }
                else -> ApiResult.Error(message, code)
            }
        } catch (e: JSONException) {
            Log.e(TAG, "JSON解析异常", e)
            ApiResult.Error("数据解析失败")
        }
    }

    private fun buildUrl(endpoint: String, params: Map<String, String> = emptyMap(), port: Int? = null): String {
        // 解析原来的 BASE_URL
        val base = if (port != null) {
            // 替换端口
            val urlParts = BASE_URL.split(":")
            "${urlParts[0]}:${urlParts[1]}:$port" + urlParts.drop(3).joinToString(":")
        } else {
            BASE_URL
        }

        val fullUrl = "$base$endpoint"

        return if (params.isNotEmpty()) {
            val queryString = params.map { "${it.key}=${it.value}" }.joinToString("&")
            "$fullUrl?$queryString"
        } else {
            fullUrl
        }
    }

    /** ====================== GET请求 ====================== */
    private suspend fun executeGet(
        endpoint: String,
        params: Map<String, String> = emptyMap(),
        needsAuth: Boolean = false,
        port: Int? = null  // 新增 port 参数

    ): ApiResult<JSONObject> {
        val url = buildUrl(endpoint, params, port)
        val request = Request.Builder().url(url).get().build()
        return executeRequest(request, needsAuth)
    }

    /** ====================== 用户认证相关 ====================== */
    fun login(account: String, password: String, callback: (Boolean, String) -> Unit) {
        // 参数校验
        if (account.isBlank() || password.isBlank()) {
            callback(false, "账号或密码不能为空")
            return
        }

        scope.launch {
            val result = executeGet(
                endpoint = "/userManager/user/login",
                params = mapOf("account" to account, "password" to password),
                needsAuth = false
            )

            withContext(Dispatchers.Main) {
                when (result) {
                    is ApiResult.Success -> {
                        val data = result.data.optJSONObject("data")
                        val message = result.data.optString("message", "")

                        when {
                            data != null -> {
                                authToken = data.optString("token")
                                userId = data.optString("id")

                                if (authToken.isNullOrEmpty() || userId.isNullOrEmpty()) {
                                    callback(false, "登录信息不完整")
                                } else {
                                    callback(true, "登录成功")
                                }
                            }
                            message.contains("无此账号") -> {
                                callback(false, "无此账号")
                            }
                            message.contains("密码错误") -> {
                                callback(false, "密码错误")
                            }
                            else -> {
                                callback(false, "登录数据异常")
                            }
                        }
                    }
                    is ApiResult.Error -> {
                        callback(false, result.message)
                    }
                }
            }

        }
    }

    fun auth(userId: String, callback: (Boolean, String, JSONObject?) -> Unit) {
        // 参数校验
        if (userId.isBlank()) {
            callback(false, "用户ID不能为空", null)
            return
        }

        if (authToken.isNullOrEmpty()) {
            callback(false, "登录状态已失效，请重新登录", null)
            return
        }

        scope.launch {
            val result = executeGet(
                endpoint = "/userManager/user/getInfo",
                params = mapOf("id" to userId),
                needsAuth = true
            )

            withContext(Dispatchers.Main) {
                when (result) {
                    is ApiResult.Success -> {
                        val data = result.data.optJSONObject("data")
                        callback(true, "鉴权成功", data)

                    }
                    is ApiResult.Error -> {
                        // 如果是401错误，清除本地认证信息
                        if (result.code == 401) {
                            clearAuthInfo()
                        }
                        callback(false, result.message, null)
                    }
                }
            }
        }
    }

    fun getUserSn(userId: String, callback: (Boolean, String, List<String>?) -> Unit) {
        scope.launch {
            // 发送 HTTP 请求
            val result = executeGet(
                endpoint = "/productManager/relationUserSn/list",
                params = mapOf("userId" to userId, "state" to "1"),
                needsAuth = false,
                port = port2
            )

            withContext(Dispatchers.Main) {
                when (result) {
                    is ApiResult.Success -> {
                        // 如果请求成功，回调返回成功信息
                        val data = result.data.optJSONArray("data")
                        if (data != null && data.length() > 0) {
                            // 创建一个 SN 列表
                            val snList = mutableListOf<String>()
                            for (i in 0 until data.length()) {
                                val snObject = data.getJSONObject(i)
                                val sn = snObject.optString("sn", null)
                                if (sn != null) {
                                    snList.add(sn)
                                }
                            }
                            callback(true, "获取成功", snList)
                        } else {
                            callback(false, "未找到数据", null)
                        }
                    }
                    is ApiResult.Error -> {
                        // 请求失败，回调返回错误信息
                        callback(false, result.message, null)
                    }
                }
            }
        }
    }



    /** ====================== 扩展接口示例 ====================== */
    fun getUserProfile(userId: String, callback: (Boolean, String, JSONObject?) -> Unit) {
        scope.launch {
            val result = executeGet(
                endpoint = "/userManager/user/profile",
                params = mapOf("id" to userId),
                needsAuth = true
            )

            withContext(Dispatchers.Main) {
                when (result) {
                    is ApiResult.Success -> {
                        callback(true, "获取成功", result.data.optJSONObject("data"))
                    }
                    is ApiResult.Error -> {
                        callback(false, result.message, null)
                    }
                }
            }
        }
    }

    private fun clearAuthInfo() {
        authToken = null
        userId = null
        Log.d(TAG, "认证信息已清除")
    }

    fun destroy() {
        scope.cancel()
        client.dispatcher.executorService.shutdown()
        clearAuthInfo()
        INSTANCE = null
    }
}