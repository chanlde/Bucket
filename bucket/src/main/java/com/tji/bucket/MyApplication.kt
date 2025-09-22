package com.tji.bucket

import android.app.Application
import com.dji.network.utils.NetWorkUtils.Companion.port3
import com.dji.network.utils.NetWorkUtils.Companion.serverIp
import com.tji.bucket.util.ToastUtils
import com.tji.network.MQTTConfig
import com.tji.network.MqttManager
import com.tji.network.TcpConnection

// 自定义 Application 类
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ToastUtils.init(applicationContext) // 传递应用程序的 context
    }

    override fun onTerminate() {
        super.onTerminate()
    }
}
