package com.tji.bucket.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.tji.bucket.R
@ExperimentalMaterial3Api
@Composable
fun CustomTopAppBar(
    title: String,
    onSettingsClick: () -> Unit = { /* 默认空操作 */ }
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            // 设置一个透明的图标，填充左侧空间
            Spacer(modifier = Modifier.width(24.dp))  // 可以根据需要调整宽度
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "设置")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorResource(id = R.color.antique_white),  // 背景色
            titleContentColor = colorResource(id = R.color.black), // 标题文字颜色
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary // 动作按钮颜色
        )
    )
}

@ExperimentalMaterial3Api
@Preview
@Composable
fun PreviewCustomTopAppBar() {
    MaterialTheme {
        CustomTopAppBar(title = "dddd", onSettingsClick = {})
    }
}
