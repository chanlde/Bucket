package com.tji.bucket.ui.components

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tji.bucket.data.model.Login
import com.tji.bucket.data.repository.AuthRepo
import com.tji.bucket.data.repository.LinkRepo
import com.tji.bucket.data.repository.SwitchRepo
import com.tji.bucket.data.viewmodel.LinkViewModel
import com.tji.bucket.data.viewmodel.LoginViewModel
import com.tji.bucket.ui.theme.LoginColors
import com.tji.bucket.util.ToastUtils
import com.tji.bucket.data.viewmodel.MainViewModel
import com.tji.bucket.data.viewmodel.SwitchViewModel
import com.tji.bucket.service.MqttEventHandler
import com.tji.bucket.service.MqttSubscriptionManager
import com.tji.bucket.util.userData
import com.tji.bucket.util.userData.updateMqttConfig

@Composable
private fun RememberMeAndForgotPassword(
    rememberMe: Boolean,
    onRememberMeChange: (Boolean) -> Unit,
    onForgotPassword: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onRememberMeChange(!rememberMe) }
        ) {
            Checkbox(
                checked = rememberMe,
                onCheckedChange = onRememberMeChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = LoginColors.Primary
                )
            )
            Text(
                text = "记住我",
                fontSize = 12.sp,
                color = LoginColors.TextSecondary,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Text(
            text = "忘记密码？",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = LoginColors.Primary,
            modifier = Modifier.clickable { onForgotPassword() }
        )
    }
}


@Composable
fun LoginWidget(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    onLogin: (Login) -> Unit = {},
    onDeveloperModeClick: () -> Unit,
    onForgotPassword: () -> Unit = {},
    viewModel: MainViewModel
) {
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var accountError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    account = ("yj_test01")
    password = ("123456")

    userData.setUserId(account)

    fun handleLogin() {
        viewModel.login(account, password, rememberMe) { loginSuccess, errorMsg ->
            if (loginSuccess) {
                onLogin(Login(account, password, rememberMe))
                updateMqttConfig(username = account)
            } else {
                if (errorMsg != null) {
                    if (errorMsg.contains("账号")) {
                        accountError = errorMsg
                        passwordError = ""
                    } else if (errorMsg.contains("密码")) {
                        passwordError = errorMsg
                        accountError = ""
                    } else {
                        // 其他错误
                        accountError = errorMsg
                        passwordError = errorMsg
                    }
                } else {
                    accountError = "登录失败，请重试"
                    passwordError = "登录失败，请重试"
                }
                ToastUtils.showToast("登录失败: $errorMsg")
            }
        }
    }

    LoginBackground(modifier = modifier) {
        LoginCard {
            LoginLayout(
                account = account,
                password = password,
                accountError = accountError,
                passwordError = passwordError,
                passwordVisible = passwordVisible,
                rememberMe = rememberMe,
                isLoading = isLoading,
                onAccountChange = {
                    account = it
                    accountError = ""
                },
                onPasswordChange = {
                    password = it
                    passwordError = ""
                },
                onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                onRememberMeChange = { rememberMe = it },
                onLogin = { handleLogin() },
                onForgotPassword = onForgotPassword,
                onDeveloperModeClick = {
                    onDeveloperModeClick()
                }
            )
        }
    }
}

@Composable
private fun LoginBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        LoginColors.Background,
                        Color(0xFFF0F4FF),
                        Color(0xFFE8F0FF)
                    )
                )
            )
    ) {
        content()
    }
}

@Composable
private fun BoxScope.LoginCard(
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenWidth = configuration.screenWidthDp.dp

    Card(
        modifier = Modifier
            .then(
                if (isLandscape) {
                    Modifier
                        .widthIn(max = minOf(screenWidth * 0.6f, 500.dp))
                        .fillMaxHeight(0.85f)
                } else {
                    Modifier
                        .fillMaxWidth(0.9f)
                        .wrapContentHeight()
                }
            )
            .padding(
                horizontal = if (isLandscape) 32.dp else 16.dp,
                vertical = if (isLandscape) 16.dp else 32.dp
            )
            .align(Alignment.Center),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = LoginColors.Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        content()
    }
}

@Composable
private fun LoginLayout(
    account: String,
    password: String,
    accountError: String,
    passwordError: String,
    passwordVisible: Boolean,
    rememberMe: Boolean,
    isLoading: Boolean,
    onAccountChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onLogin: () -> Unit,
    onDeveloperModeClick: () -> Unit,
    onForgotPassword: () -> Unit
    ) {
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        if (isLandscape) {
            // 横屏布局
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LoginFormContent(
                    account = account,
                    password = password,
                    accountError = accountError,
                    passwordError = passwordError,
                    passwordVisible = passwordVisible,
                    rememberMe = rememberMe,
                    isLoading = isLoading,
                    onAccountChange = onAccountChange,
                    onPasswordChange = onPasswordChange,
                    onPasswordVisibilityToggle = onPasswordVisibilityToggle,
                    onRememberMeChange = onRememberMeChange,
                    onLogin = onLogin,
                    onForgotPassword = onForgotPassword,
                    isLandscape = true,
                    onDeveloperModeClick = onDeveloperModeClick

                )
            }
        }
    } else {
        // 竖屏布局
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LoginFormContent(
                account = account,
                password = password,
                accountError = accountError,
                passwordError = passwordError,
                passwordVisible = passwordVisible,
                rememberMe = rememberMe,
                isLoading = isLoading,
                onAccountChange = onAccountChange,
                onPasswordChange = onPasswordChange,
                onPasswordVisibilityToggle = onPasswordVisibilityToggle,
                onRememberMeChange = onRememberMeChange,
                onLogin = onLogin,
                onForgotPassword = onForgotPassword,
                isLandscape = false,
                onDeveloperModeClick = onDeveloperModeClick

            )
        }
    }
}

@Composable
private fun LoginFormContent(
    account: String,
    password: String,
    accountError: String,
    passwordError: String,
    passwordVisible: Boolean,
    rememberMe: Boolean,
    isLoading: Boolean,
    onAccountChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onLogin: () -> Unit,
    onDeveloperModeClick: () -> Unit,
    onForgotPassword: () -> Unit,
    isLandscape: Boolean
) {
    LoginForm(
        account = account,
        password = password,
        accountError = accountError,
        passwordError = passwordError,
        passwordVisible = passwordVisible,
        rememberMe = rememberMe,
        isLoading = isLoading,
        onAccountChange = onAccountChange,
        onPasswordChange = onPasswordChange,
        onPasswordVisibilityToggle = onPasswordVisibilityToggle,
        onRememberMeChange = onRememberMeChange,
        onLogin = onLogin,
        onForgotPassword = onForgotPassword,
        isLandscape = isLandscape,
        onDeveloperModeClick = onDeveloperModeClick
    )
}

// Logo 或标题 - 横屏时缩小
@Composable
fun LogoOrTitle(isLandscape: Boolean) {
    Text(
        text = "登录",
        style = if (isLandscape)
            MaterialTheme.typography.headlineMedium
        else
            MaterialTheme.typography.headlineLarge,
        color = LoginColors.Primary,
        modifier = Modifier.padding(
            bottom = if (isLandscape) 8.dp else 16.dp
        )
    )
}

// 登录按钮
@Composable
fun LoginButton(
    isLoading: Boolean,
    onLogin: () -> Unit,
    isLandscape: Boolean
) {
    Button(
        onClick = onLogin,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth(0.5f)
            .height(if (isLandscape) 45.dp else 50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = LoginColors.Primary
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = "登录",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }
    }
}

@Composable
fun LoginForm(
    account: String,
    password: String,
    accountError: String,
    passwordError: String,
    passwordVisible: Boolean,
    rememberMe: Boolean,
    isLoading: Boolean,
    onAccountChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onLogin: () -> Unit,
    onDeveloperModeClick: () -> Unit,
    onForgotPassword: () -> Unit,
    isLandscape: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            if (isLandscape) 12.dp else 16.dp
        )
    ) {
        LogoOrTitle(isLandscape)

        InputField(
            value = account,
            onValueChange = onAccountChange,
            isError = accountError.isNotEmpty(),
            errorMessage = accountError
        )

        InputField(
            value = password,
            onValueChange = onPasswordChange,
            isError = passwordError.isNotEmpty(),
            errorMessage = passwordError,
            isPassword = true,
            passwordVisible = passwordVisible,
            onPasswordVisibilityToggle = onPasswordVisibilityToggle
        )
        RememberMeAndForgotPassword(rememberMe, onRememberMeChange, onForgotPassword)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center // 设置按钮间距
        ) {
            // 登录按钮
            LoginButton(isLoading, onLogin, isLandscape)

            // 开发者模式按钮
            TextButton(
                onClick = onDeveloperModeClick,
                modifier = Modifier
                    .align(Alignment.CenterVertically) // 垂直居中
            ) {
                Text(
                    text = "Wifi模式",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Gray.copy(alpha = 0.5f) // 设置透明文字
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable

fun LoginWidgetPreview() {
    val fakeLinkRepository = LinkRepo()
    val fakeSwitchRepository = SwitchRepo()
    val authRepository = AuthRepo()
    val mqttEventHandler = MqttEventHandler(fakeLinkRepository)

    val mainViewModel = MainViewModel(
        LinkViewModel(fakeLinkRepository),
        SwitchViewModel(fakeSwitchRepository),
        LoginViewModel(authRepository),
        MqttSubscriptionManager(mqttEventHandler)

    )

    LoginWidget(
        isLoading = false,
        onLogin = { /* Handle login */ },
        onDeveloperModeClick = { /* Handle developer mode click */ },
        viewModel = mainViewModel
    )

}