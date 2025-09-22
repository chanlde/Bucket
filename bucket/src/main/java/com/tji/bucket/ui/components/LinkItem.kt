package com.tji.bucket.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tji.bucket.data.model.LinkDevice
import com.tji.bucket.data.model.Switch
import com.tji.bucket.data.repository.AuthRepo
import com.tji.bucket.data.repository.LinkRepo
import com.tji.bucket.data.repository.SwitchRepo
import com.tji.bucket.data.viewmodel.LinkViewModel
import com.tji.bucket.data.viewmodel.LoginViewModel
import com.tji.bucket.data.viewmodel.MainViewModel
import com.tji.bucket.data.viewmodel.SwitchViewModel
import com.tji.bucket.ui.main.SwitchItemComposable

// LinkItem 组件
@Composable
fun LinkItem(
    link: LinkDevice,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
            hoveredElevation = 8.dp
        ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        )
    ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = link.deviceName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A1A1A)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusChip(isOnline = link.isOnline)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "序列号: ${link.serial_number}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF666666)
                    )
                )

                Text(
                    text = "开关数量: ${link.subDevices.size}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF666666)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Switch 列表（竖屏/横屏）
                if (isPortrait) {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 400.dp)
                    ) {
                        items(link.subDevices) { switch ->
                            SwitchItemComposable(link.serial_number, switch = switch, viewModel = viewModel)
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 400.dp)
                    ) {
                        items(link.subDevices) { switch ->
                            SwitchItemComposable(link.serial_number, switch = switch, viewModel = viewModel)
                        }
                    }
                }
            }
        }
}

@Preview(showBackground = true)
@Composable
fun LinkItemPreview() {
    // 模拟 LinkDevice 数据
    val mockLink = LinkDevice(
        event_type = "device_status",
        serial_number = "TJI001",
        deviceName = "客厅控制器",
        deviceType = "Controller",
        manufacturer = "TJI",
        deviceModel = "TJI-C100",
        isOnline = true,
        hwVersion = "1.2",
        swVersion = "2.1.0",

        uptime = 86400,
        deviceConfig = "",
        subDevices = listOf(
            Switch(
                serialNumber = "客厅-SWITCH1",
                deviceName = "客厅 舵机控制器 1",
                deviceType = "HydroSwitch",
                isOnline = true,
                currentAngle = 15.0,
                currentCurrent = 110.0,
                inputVoltage = 12.0,
                servoMinAngle = 0.0,
                servoMaxAngle = 180.0,
                uptime = 100
            ),
            Switch(
                serialNumber = "客厅-SWITCH2",
                deviceName = "客厅 舵机控制器 2",
                deviceType = "HydroSwitch",
                isOnline = false,
                currentAngle = 30.0,
                currentCurrent = 120.0,
                inputVoltage = 12.0,
                servoMinAngle = 0.0,
                servoMaxAngle = 180.0,
                uptime = 200
            ),
            Switch(
                serialNumber = "客厅-SWITCH3",
                deviceName = "客厅 舵机控制器 3",
                deviceType = "HydroSwitch",
                isOnline = true,
                currentAngle = 45.0,
                currentCurrent = 130.0,
                inputVoltage = 12.0,
                servoMinAngle = 0.0,
                servoMaxAngle = 180.0,
                uptime = 300
            )
        ),
        timestamp = System.currentTimeMillis().toString()
    )

    // 模拟 MainViewModel
    val fakeLinkRepository = LinkRepo()
    val fakeSwitchRepository = SwitchRepo()
    val authRepository = AuthRepo()
    val mainViewModel = MainViewModel(
        LinkViewModel(fakeLinkRepository),
        SwitchViewModel(fakeSwitchRepository),
        LoginViewModel(authRepository)
    )

    MaterialTheme {
        LinkItem(
            link = mockLink,
            viewModel = mainViewModel
        )
    }
}

//// StatusChip（复用 SwitchItem 的实现）
//@Composable
//fun StatusChip(isOnline: Boolean) {
//    val text = if (isOnline) "Online" else "Offline"
//    val color = if (isOnline) Color.Green else Color.Red
//    Box(
//        modifier = Modifier
//            .background(color, shape = RoundedCornerShape(4.dp))
//            .padding(horizontal = 8.dp, vertical = 4.dp)
//    ) {
//        Text(
//            text = text,
//            color = Color.White,
//            style = MaterialTheme.typography.bodySmall
//        )
//    }
//}