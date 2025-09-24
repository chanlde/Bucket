package com.tji.bucket

import android.app.Application
import com.tji.bucket.util.ToastUtils
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ToastUtils.init(applicationContext) // 传递应用程序的 context
    }

    override fun onTerminate() {
        super.onTerminate()
    }
}
