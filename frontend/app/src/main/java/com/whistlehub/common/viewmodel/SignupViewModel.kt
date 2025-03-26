package com.whistlehub.common.viewmodel

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whistlehub.common.data.remote.dto.request.AuthRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.AuthResponse
import com.whistlehub.common.data.repository.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// 회원가입 상태를 표현하는 sealed class
sealed class SignUpState {
    object Idle : SignUpState()
    object Loading : SignUpState()
    data class Success(val message: String? = null) : SignUpState()
    data class Error(val message: String) : SignUpState()
}

// 이메일 인증 상태를 표현하는 sealed class (같은 파일 내에 정의)
sealed class EmailVerificationState {
    object Idle : EmailVerificationState()
    object Sending : EmailVerificationState() // 인증 코드 전송 중
    data class Sent(val message: String = "인증 코드가 전송되었습니다.") : EmailVerificationState()
    data class Verified(val message: String = "인증 성공") : EmailVerificationState()
    data class Error(val message: String) : EmailVerificationState()
}

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authService: AuthService
) : ViewModel() {

    private val _signUpState = MutableStateFlow<SignUpState>(SignUpState.Idle)
    val signUpState: StateFlow<SignUpState> = _signUpState

    private val _emailVerificationState = MutableStateFlow<EmailVerificationState>(EmailVerificationState.Idle)
    val emailVerificationState: StateFlow<EmailVerificationState> = _emailVerificationState

    // 아이디 중복 확인 API 호출
    fun checkDuplicateId(loginId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _signUpState.value = SignUpState.Loading
            try {
                val response = authService.checkDuplicateId(loginId)
                if (response.code == "SU") {
                    // payload가 true이면 이미 존재하는 아이디
                    onResult(response.payload ?: false)
                    _signUpState.value = SignUpState.Idle
                } else {
                    _signUpState.value = SignUpState.Error(response.message ?: "아이디 중복 확인에 실패했습니다.")
                }
            } catch (e: Exception) {
                _signUpState.value = SignUpState.Error("예외 발생: ${e.message}")
            }
        }
    }

    // 이메일 중복 확인 API 호출
    fun checkDuplicateEmail(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _signUpState.value = SignUpState.Loading
            try {
                val response = authService.checkDuplicateEmail(email)
                if (response.code == "SU") {
                    onResult(response.payload ?: false)
                    _signUpState.value = SignUpState.Idle
                } else {
                    _signUpState.value = SignUpState.Error(response.message ?: "이메일 중복 확인에 실패했습니다.")
                }
            } catch (e: Exception) {
                _signUpState.value = SignUpState.Error("예외 발생: ${e.message}")
            }
        }
    }

    // 이메일 인증 코드 요청 API 호출
    fun sendEmailVerification(email: String) {
        viewModelScope.launch {
            _emailVerificationState.value = EmailVerificationState.Sending
            try {
                val response = authService.sendEmailVerification(email)
                if (response.code == "SU") {
                    _emailVerificationState.value = EmailVerificationState.Sent()
                } else {
                    _emailVerificationState.value = EmailVerificationState.Error(response.message ?: "인증 코드 전송에 실패했습니다.")
                }
            } catch (e: Exception) {
                _emailVerificationState.value = EmailVerificationState.Error("예외 발생: ${e.message}")
            }
        }
    }

    // 이메일 인증 코드 검증 API 호출
    fun validateEmailCode(email: String, code: String) {
        viewModelScope.launch {
            _emailVerificationState.value = EmailVerificationState.Sending
            try {
                val request = AuthRequest.ValidateEmailRequest(email, code)
                val response = authService.validateEmailCode(request)
                if (response.code == "SU" && (response.payload ?: false)) {
                    _emailVerificationState.value = EmailVerificationState.Verified()
                } else {
                    _emailVerificationState.value = EmailVerificationState.Error(response.message ?: "인증 코드 검증에 실패했습니다.")
                }
            } catch (e: Exception) {
                _emailVerificationState.value = EmailVerificationState.Error("예외 발생: ${e.message}")
            }
        }
    }


    // 닉네임 중복 확인 API 호출
    fun checkDuplicateNickname(nickname: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _signUpState.value = SignUpState.Loading
            try {
                val response = authService.checkDuplicateNickname(nickname)
                if (response.code == "SU") {
                    onResult(response.payload ?: false)
                    _signUpState.value = SignUpState.Idle
                } else {
                    _signUpState.value = SignUpState.Error(response.message ?: "닉네임 중복 확인에 실패했습니다.")
                }
            } catch (e: Exception) {
                _signUpState.value = SignUpState.Error("예외 발생: ${e.message}")
            }
        }
    }

    // 태그 목록 API 호출
    fun getTagList(onResult: (List<AuthResponse.TagResponse>) -> Unit) {
        viewModelScope.launch {
            _signUpState.value = SignUpState.Loading
            try {
                val response = authService.getTagList()
                if (response.code == "SU") {
                    onResult(response.payload ?: emptyList())
                    _signUpState.value = SignUpState.Idle
                } else {
                    _signUpState.value = SignUpState.Error(response.message ?: "태그 목록 요청에 실패했습니다.")
                }
            } catch (e: Exception) {
                _signUpState.value = SignUpState.Error("예외 발생: ${e.message}")
            }
        }
    }

    // 회원가입 API 호출
    fun register(
        loginId: String,
        password: String,
        email: String,
        nickname: String,
        birth: String,
        gender: Char,
        tags: List<Int>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _signUpState.value = SignUpState.Loading
            try {
                val request = AuthRequest.RegisterRequest(loginId, password, email, nickname, gender, birth, tags)
                val response = authService.register(request)
                if (response.code == "SU" && response.payload != null) {
                    _signUpState.value = SignUpState.Success("회원가입에 성공했습니다.")
                    onSuccess()
                } else {
                    _signUpState.value = SignUpState.Error(response.message ?: "회원가입에 실패했습니다.")
                }
            } catch (e: Exception) {
                _signUpState.value = SignUpState.Error("예외 발생: ${e.message}")
            }
        }
    }

    // 상태 초기화 함수
    fun resetState() {
        _signUpState.value = SignUpState.Idle
    }
}
