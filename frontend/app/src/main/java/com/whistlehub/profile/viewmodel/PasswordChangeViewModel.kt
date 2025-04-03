package com.whistlehub.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whistlehub.common.data.remote.dto.request.ProfileRequest
import com.whistlehub.common.data.repository.ProfileService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasswordChangeViewModel @Inject constructor(
    private val profileService: ProfileService
) : ViewModel() {

    private val _uiState = MutableStateFlow(PasswordChangeUiState())
    val uiState: StateFlow<PasswordChangeUiState> = _uiState.asStateFlow()

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")

                val request = ProfileRequest.ChangePasswordRequest(
                    oldPassword = currentPassword,
                    newPassword = newPassword
                )

                val response = profileService.changePassword(request)

                if (response.code == "SU") {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showSuccessDialog = true,
                        dialogMessage = "비밀번호가 성공적으로 변경되었습니다."
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = response.message ?: "비밀번호 변경에 실패했습니다."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "네트워크 오류가 발생했습니다."
                )
            }
        }
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(showSuccessDialog = false)
    }
}

data class PasswordChangeUiState(
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val showSuccessDialog: Boolean = false,
    val dialogMessage: String = ""
)
