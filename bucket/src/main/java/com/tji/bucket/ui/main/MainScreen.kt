package com.tji.bucket.ui.main

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dji.network.DataReportManager
import com.tji.bucket.data.model.ControlMode
import com.tji.bucket.data.model.Switch
import com.tji.bucket.data.model.SwitchControlParms
import com.tji.bucket.data.repository.AuthRepo
import com.tji.bucket.data.repository.LinkRepo
import com.tji.bucket.data.repository.SwitchRepo
import com.tji.bucket.data.viewmodel.LinkViewModel
import com.tji.bucket.data.viewmodel.LoginViewModel
import com.tji.bucket.data.viewmodel.MainViewModel
import com.tji.bucket.data.viewmodel.SwitchViewModel
import com.tji.bucket.ui.components.SwitchItem
import com.tji.bucket.ui.components.CustomTopAppBar
import com.tji.bucket.ui.components.LinkItem
import com.tji.bucket.util.userData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onBack: (() -> Unit)? = null,
) {
    val loginUiState by viewModel.loginViewModel.uiState.collectAsState()
    val links by viewModel.links.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // 加载用户数据
    LaunchedEffect(loginUiState.userId) {
        Log.d("MainScreen", "UserId: ${loginUiState.userId}")
        if (loginUiState.userId != null) {
            viewModel.loadLinks(loginUiState.userId!!)
        }
    }

    // UI 渲染
    Scaffold(
        topBar = { CustomTopAppBar("水桶控制") }
    ) { padding ->
        when {
            isLoading -> CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            )
            links.isEmpty() -> Text(
                text = "没有可用的 Link 设备",
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center),
                style = MaterialTheme.typography.bodyLarge
            )
            else -> {
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(links) { link ->
                        LinkItem(link = link, viewModel = viewModel)
                    }
                }

            }
        }
    }

    BackHandler {
        onBack?.invoke()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
}


@Composable
fun SwitchItemComposable(linkSn: String, switch: Switch, viewModel: MainViewModel) {
    var scParms by remember {
        mutableStateOf(
            SwitchControlParms(
                sn = switch.serialNumber,
                angle = switch.currentAngle.toInt(),
                speed = 10,
                mode = ControlMode.ABSOLUTE
            )
        )
    }
    SwitchItem(
        linkSn = linkSn,
        switch = switch,
        scParms = scParms,
        onControl = { updatedParms ->
            viewModel.setSwitchAngle(linkSn, updatedParms)
        },
        onAngleChange = { newAngle ->
            scParms = scParms.copy(angle = newAngle)
        }
    )
}