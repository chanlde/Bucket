package com.tji.bucket.ui.main

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.tji.bucket.ui.theme.BucketTheme
import com.tji.bucket.util.MainViewModelFactory
import com.tji.bucket.data.repository.AuthRepo
import com.tji.bucket.data.repository.LinkRepo
import com.tji.bucket.data.repository.SwitchRepo
import com.tji.bucket.ui.components.LoginWidget
import kotlin.jvm.java
import com.tji.bucket.data.viewmodel.MainViewModel
import com.tji.bucket.service.MqttService
import androidx.compose.runtime.LaunchedEffect
import com.tji.bucket.webControl.WebViewScreen
import kotlinx.coroutines.delay
@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private var isServiceStarted = false // 标记服务是否已启动

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val deviceRepository = LinkRepo()
        val switchRepository = SwitchRepo()
        val authRepository = AuthRepo()

        val mainViewModel: MainViewModel by viewModels(factoryProducer = {
            MainViewModelFactory(deviceRepository, switchRepository, authRepository)
        })

        setContent {
            var appState by remember { mutableStateOf("login") }
            val context = LocalContext.current // 获取 MainActivity 的上下文

            MaterialTheme {
                when (appState) {
                    "developer" -> {
                        WebViewScreen(
                            url = "http://192.168.5.1/index.html",
                            onBack = { appState = "login" }
                        )
                    }
                    "main" -> {
                        LaunchedEffect(Unit) {
                            if (!isServiceStarted) {
                                val serviceIntent = Intent(context, MqttService::class.java)
                                context.startService(serviceIntent)
                                isServiceStarted = true
                                Log.d("MainActivity", "MqttService 延迟启动成功")
                            } else {
                                Log.d("MainActivity", "MqttService 已启动，无需重复启动")
                            }
                        }

                        MainScreen(
                            viewModel = mainViewModel,
                            onBack = {
                                // 退出 main 时停止服务
                                val serviceIntent = Intent(context, MqttService::class.java)
                                context.stopService(serviceIntent)
                                isServiceStarted = false
                                Log.d("MainActivity", "MqttService 已停止")
                                appState = "login"
                            }
                        )
                    }
                    else -> {
                        LoginWidget(
                            onLogin = { loginData -> appState = "main" },
                            viewModel = mainViewModel,
                            onDeveloperModeClick = { appState = "developer" }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 确保 Activity 销毁时停止服务
        if (isServiceStarted) {
            val serviceIntent = Intent(this, MqttService::class.java)
            stopService(serviceIntent)
            isServiceStarted = false
            Log.d("MainActivity", "MqttService 在 Activity 销毁时停止")
        }
    }
}

