package com.whistlehub.common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whistlehub.common.data.repository.AuthService
import com.whistlehub.common.data.remote.dto.request.AuthRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.AuthResponse
import com.whistlehub.common.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// 로그인 상태를 표현하는 sealed class
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authService: AuthService, // 변경: AuthService 주입
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    // 로그인 처리 함수 (입력값 유효성 검사 포함)
    fun login(loginId: String, password: String) {
        // 간단한 입력값 유효성 검사
        if (loginId.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("아이디와 비밀번호를 모두 입력해주세요.")
            return
        }

        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            try {
                val request = AuthRequest.LoginRequest(loginId, password)
                // AuthService의 login() 호출 (이미 executeApiCall 사용)
                val response: ApiResponse<AuthResponse.LoginResponse> = authService.login(request)
                if (response.code == "SU" && response.payload != null) {
                    // 토큰 저장
                    val payload = response.payload
                    tokenManager.saveTokens(payload.accessToken, payload.refreshToken)
                    _loginState.value = LoginState.Success
                } else {
                    _loginState.value = LoginState.Error(response.message)
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "알 수 없는 오류 발생")
            }
        }
    }

    // 필요에 따라 상태를 초기화할 함수
    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}
