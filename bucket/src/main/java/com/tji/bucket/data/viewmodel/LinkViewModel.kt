package com.tji.bucket.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tji.bucket.data.model.DeviceUiState
import com.tji.bucket.data.repository.LinkRepository
import com.tji.bucket.data.vminterface.LinkViewModelInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LinkViewModel(private val repository: LinkRepository) : ViewModel(),
    LinkViewModelInterface {
    private val _uiState = MutableStateFlow(DeviceUiState())
    override val uiState: StateFlow<DeviceUiState> = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    override val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    override fun loadSwitchDevices(sn: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val devices = repository.getSwitchFormLink(sn)
                _uiState.value = _uiState.value.copy(switchDevices = devices, errorMessage = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "加载失败: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    override fun refreshDevices(sn: String) {
        loadSwitchDevices(sn)
    }

    override fun clearErrorMessage() {
        _errorMessage.value = null
    }


}