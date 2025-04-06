package com.whistlehub.common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whistlehub.common.data.remote.dto.request.AuthRequest
import com.whistlehub.common.data.repository.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * PasswordResetState는 비밀번호 초기화 과정의 상태를 나타내기 위한 sealed class입니다.
 * - Idle: 초기 상태, 아무런 작업도 수행하지 않는 상태
 * - Loading: 비밀번호 초기화 요청 중 (로딩중)인 상태
 * - Success: 비밀번호 초기화 메일 발송 성공 상태
 * - Error: 비밀번호 초기화 실패 또는 오류 발생 시 에러 메시지와 함께 전달되는 상태
 */
sealed class PasswordResetState {
    object Idle : PasswordResetState()
    object Loading : PasswordResetState()
    data class Success(val message: String? = null) : PasswordResetState()
    data class Error(val message: String) : PasswordResetState()
}

/**
 * PasswordResetViewModel은 사용자 비밀번호 초기화와 관련된 비즈니스 로직을 처리합니다.
 *
 * 주요 기능:
 * - 사용자 ID와 이메일 유효성 검사
 * - 비밀번호 초기화 요청 API 호출
 * - 비밀번호 초기화 과정 상태 관리
 */
@HiltViewModel
class PasswordResetViewModel @Inject constructor(
    private val authService: AuthService
) : ViewModel() {

    // 비밀번호 초기화 상태를 관리하는 StateFlow
    private val _resetState = MutableStateFlow<PasswordResetState>(PasswordResetState.Idle)
    val resetState: StateFlow<PasswordResetState> = _resetState

    /**
     * 비밀번호 초기화 요청 함수
     * 사용자 ID와 이메일을 받아 비밀번호 초기화 API를 호출합니다.
     *
     * @param userId 사용자 ID
     * @param email 사용자 이메일
     */
    fun resetPassword(userId: String, email: String) {
        // 입력값 유효성 검사
        if (userId.isBlank() || email.isBlank()) {
            _resetState.value = PasswordResetState.Error("아이디와 이메일을 모두 입력해주세요.")
            return
        }

        // 이메일 형식 검사
        if (!isValidEmail(email)) {
            _resetState.value = PasswordResetState.Error("올바른 이메일 형식이 아닙니다.")
            return
        }

        _resetState.value = PasswordResetState.Loading

        viewModelScope.launch {
            try {
                // 비밀번호 초기화 API 호출
                // 요청 객체에 이메일과 로그인 아이디를 함께 전달
                val request = AuthRequest.ResetPasswordRequest(email, userId) // 이메일로 임시 비밀번호 발송
                val response = authService.resetPassword(request)

                if (response.code == "SU") {
                    _resetState.value = PasswordResetState.Success("임시 비밀번호가 이메일로 발송되었습니다. 이메일을 확인해주세요.")
                } else {
                    // 특정 에러 코드에 따른 메시지 처리
                    val errorMessage = when(response.code) {
                        "NMIE" -> "아이디와 이메일이 일치하지 않습니다."
                        "ESF" -> "이메일 발송에 실패하였습니다. 잠시 후 다시 시도해주세요."
                        else -> response.message ?: "비밀번호 초기화 요청 실패"
                    }
                    _resetState.value = PasswordResetState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _resetState.value = PasswordResetState.Error(e.message ?: "알 수 없는 오류 발생")
            }
        }
    }

    /**
     * 이메일 형식 검사 함수
     * 기본적인 이메일 형식(@와 .이 포함된)인지 확인합니다.
     */
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * 상태를 초기화하는 함수
     * 필요에 따라 상태를 Idle로 초기화합니다.
     */
    fun resetState() {
        _resetState.value = PasswordResetState.Idle
    }
}