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
                    // 에러 응답을 다이얼로그로 표시하도록 변경
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showErrorDialog = true,
                        errorMessage = response.message ?: "비밀번호 변경에 실패했습니다."
                    )
                }
            } catch (e: Exception) {
                // 예외 발생 시에도 다이얼로그로 표시
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showErrorDialog = true,
                    errorMessage = e.message ?: "네트워크 오류가 발생했습니다."
                )
            }
        }
    }

    // 성공 다이얼로그 닫기
    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(showSuccessDialog = false)
    }

    // 오류 다이얼로그 닫기
    fun dismissErrorDialog() {
        _uiState.value = _uiState.value.copy(showErrorDialog = false)
    }

    // 오류 다이얼로그 표시 (클라이언트 측 유효성 검사 실패 등)
    fun showErrorDialog(message: String) {
        _uiState.value = _uiState.value.copy(
            showErrorDialog = true,
            errorMessage = message
        )
    }
}

data class PasswordChangeUiState(
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val showSuccessDialog: Boolean = false,
    val showErrorDialog: Boolean = false,
    val dialogMessage: String = ""
)