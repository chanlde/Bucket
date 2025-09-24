package com.tji.bucket.util

import android.content.Context
import android.widget.Toast
import com.tji.bucket.BuildConfig

object ToastUtils {
    // 用于存储应用的上下文，以便在任何地方使用
    private lateinit var appContext: Context

    // 获取版本名称（从 BuildConfig 中获取）
    private val versionName = BuildConfig.VERSION_NAME  // 从 BuildConfig 获取版本名称
    // 获取版本号（从 BuildConfig 中获取）
    private val versionCode = BuildConfig.VERSION_CODE  // 从 BuildConfig 获取版本号

    /**
     * 初始化方法，用于设置应用上下文。
     * 需要在应用启动时调用一次，通常在 Application 类中调用。
     */
    fun init(context: Context) {
        // 使用 applicationContext，确保在应用中任何地方都能访问该上下文，而不是某个 Activity 的上下文
        appContext = context.applicationContext
    }

    /**
     * 显示短时间的 Toast 消息。
     * @param message 需要显示的消息内容
     */
    fun showToast(message: String) {
        // 使用应用上下文显示 Toast
        Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * 获取应用的版本名称。
     * @return 返回版本名称，例如 "1.0.0"
     */
    fun getVersionName(): String {
        return versionName
    }

    /**
     * 获取应用的版本号。
     * @return 返回版本号，例如 1
     */
    fun getVersionCode(): Int {
        return versionCode
    }
}
