package com.tji.bucket.data.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dji.network.DataReportManager
import com.tji.bucket.data.model.LinkDevice
import com.tji.bucket.data.vminterface.LoginUiState
import com.tji.bucket.data.repository.AuthRepository
import com.tji.bucket.data.vminterface.LoginViewModelInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel(),
    LoginViewModelInterface {

    companion object {
        private const val TAG = "LoginViewModel"
    }

    private val _uiState = MutableStateFlow(LoginUiState())
    override val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // LoginViewModel
    override fun login(account: String, password: String, rememberMe: Boolean, callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                // 调用 login suspend 函数
                val loginResult = authRepository.login(account, password)
                if (loginResult.success) {
                    Log.d(TAG, "登录成功")
                    val userId = DataReportManager.getInstance().userId?.toString() ?: "unknown"
                    // 调用 auth suspend 函数
                    val authResult = authRepository.auth(userId)
                    if (authResult.success) {
                        Log.d(TAG, "认证成功")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            userId = userId,
                            errorMessage = null
                        )
                        callback(true, null)
                    } else {
                        Log.e(TAG, "认证失败: ${authResult.message}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "认证失败: ${authResult.message}"
                        )
                        callback(false, "认证失败: ${authResult.message}")
                    }
                } else {
                    Log.e(TAG, "登录失败: ${loginResult.message}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = loginResult.message
                    )
                    callback(false, loginResult.message)
                }
            } catch (e: Exception) {
                Log.e(TAG, "登录或认证异常: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
                callback(false, e.message)
            }
        }
    }

    override fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = LoginUiState()
            Log.d(TAG, "用户登出")
        }
    }

    override fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override suspend fun getAllLinks(): List<String> {
        val links = authRepository.getAllLinks()
        Log.d("LinkViewModel", "All links: $links")
        return links
    }
}