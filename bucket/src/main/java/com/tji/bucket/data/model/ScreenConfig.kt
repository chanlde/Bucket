package com.tji.bucket.data.model

import androidx.compose.ui.unit.Dp

// 屏幕配置数据类
data class ScreenConfig(
    val isLandscape: Boolean,
    val screenWidth: Dp,
    val screenHeight: Dp
)