package com.tji.bucket.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tji.bucket.data.model.SwitchControlParms
import com.tji.bucket.data.repository.SwitchRepository
import com.tji.bucket.data.vminterface.SwitchUiState
import com.tji.bucket.data.vminterface.SwitchViewModelInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SwitchViewModel(private val repository: SwitchRepository) : ViewModel(),
    SwitchViewModelInterface {
    private val _uiState = MutableStateFlow(SwitchUiState())
    override val uiState: StateFlow<SwitchUiState> = _uiState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    override val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    override fun setAngle(linkSn:String,scParms:SwitchControlParms) {
        viewModelScope.launch {
            try {
                repository.setAngle(linkSn,scParms)

                _uiState.value = _uiState.value.copy(errorMessage = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "设置角度失败: ${e.message}")
            }
        }
    }

    override fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
