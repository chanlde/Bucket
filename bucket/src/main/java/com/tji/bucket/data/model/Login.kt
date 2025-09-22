package com.tji.bucket.data.model

data class Login(
    val account: String,
    val password: String,
    val rememberMe: Boolean
)

// 定义结果数据类
data class loginResult(val success: Boolean, val message: String?)

data class AuthResult(val success: Boolean, val message: String?, val userData: Any?)