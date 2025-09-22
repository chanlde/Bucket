package com.tji.bucket.ui.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tji.bucket.data.model.Switch
import com.tji.bucket.R
import com.tji.bucket.data.model.ControlMode
import com.tji.bucket.data.model.SwitchControlParms

@Composable
fun SwitchItem(
    linkSn : String,
    switch: Switch,
    scParms: SwitchControlParms,
    onControl: (SwitchControlParms) -> Unit,
    onAngleChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var angle by remember { mutableFloatStateOf(switch.currentAngle?.toFloat() ?: scParms.angle?.toFloat() ?: 30f) }

    fun updateAngleAndControl(newAngle: Float) {
        angle = newAngle
        scParms.angle = newAngle.toInt()
        onControl(scParms)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = switch.deviceName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusChip(switch.isOnline)
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DeviceInfoButton(switch = switch, modifier = modifier)
                }
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(5.dp))
            AngleSlider(
                value = angle,
                onValueChange = { newAngle ->
                    angle = newAngle
                    scParms.angle = newAngle.toInt()
                    onAngleChange(newAngle.toInt())
                    Log.d("SwitchItem", "滑块角度更新: $linkSn,$newAngle, scParms.angle: ${scParms.angle}")
                },
                modifier = Modifier.fillMaxWidth(),
                minValue = 0f,
                maxValue = 90f
            )
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ) {
            Button(onClick = { updateAngleAndControl(90f) }) {
                Text("打开")
            }
            Spacer(modifier = Modifier.width(50.dp))
            Button(onClick = { updateAngleAndControl(0f) }) {
                Text("关闭")
            }
        }
    }
}

@Composable
fun AngleSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    minValue: Float = 0f,
    maxValue: Float = 90f
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "阀门角度:",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${value.toInt()}°",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically  // 添加垂直居中对齐
        ) {
            Text(
                text = "${minValue.toInt()}°",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            CustomSlider(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)  // 关键：让滑块占据剩余空间
                    .padding(horizontal = 20.dp)
            )

            Text(
                text = "${maxValue.toInt()}°",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// 预览 Composable
@Preview(showBackground = true)
@Composable
fun SwitchItemPreview() {
    MaterialTheme {
        val switch = Switch(
            serialNumber = "HD20240101002",
            deviceName = "Servo-Controller-01",
            deviceType = "HydroSwitch",
            isOnline = true,
            currentAngle = 45.0,
            currentCurrent = 120.5,
            inputVoltage = 12.0,
            servoMinAngle = 0.0,
            servoMaxAngle = 180.0,
            uptime = 118
        )
        SwitchItem(
            linkSn = "dddddddddddddddd",
            switch = switch,
            scParms = SwitchControlParms(
                sn = switch.serialNumber,
                angle = switch.currentAngle.toInt(), // 转换为 Int
                speed = 10,
                mode = ControlMode.ABSOLUTE
            ),
            onControl = { parms ->
                // 模拟控制操作
                println("Control: ${parms.angle}, ${parms.mode}")
            },
            onAngleChange = { angle ->
                // 模拟角度变化
                println("Angle changed to: $angle")
            }
        )
    }
}