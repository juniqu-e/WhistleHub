package com.whistlehub.common.viewmodel

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whistlehub.common.data.local.entity.UserEntity
import com.whistlehub.common.data.local.room.UserRepository
import com.whistlehub.common.data.remote.dto.request.AuthRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.AuthResponse
import com.whistlehub.common.data.repository.AuthService
import com.whistlehub.common.util.TokenManager
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

// 이메일 인증 상태를 표현하는 sealed class
sealed class EmailVerificationState {
    object Idle : EmailVerificationState()
    object Sending : EmailVerificationState() // 인증 코드 전송 중
    data class Sent(val message: String = "인증 코드가 전송되었습니다.") : EmailVerificationState()
    data class Verified(val message: String = "인증 성공") : EmailVerificationState()
    data class Error(val message: String) : EmailVerificationState()
}

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authService: AuthService,
    private val tokenManager: TokenManager,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _signUpState = MutableStateFlow<SignUpState>(SignUpState.Idle)
    val signUpState: StateFlow<SignUpState> = _signUpState

    private val _emailVerificationState = MutableStateFlow<EmailVerificationState>(EmailVerificationState.Idle)
    val emailVerificationState: StateFlow<EmailVerificationState> = _emailVerificationState

    private val _userInfo = MutableStateFlow<UserEntity?>(null)
    val userInfo: StateFlow<UserEntity?> = _userInfo

    // 인증 요청이 한 번이라도 성공했는지 여부를 판별하는 계산 프로퍼티
    val isEmailVerificationRequested: Boolean
        get() = _emailVerificationState.value is EmailVerificationState.Sent ||
                _emailVerificationState.value is EmailVerificationState.Verified

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
                    // 요청이 성공하면 Sent 상태로 변경
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
                if ((response.code == "SU") || response.code == "AVE") {
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

    fun registerAndAutoLogin(
        loginId: String,
        password: String,
        email: String,
        nickname: String,
        birth: String,
        gender: Char,
        tagList: List<Int>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _signUpState.value = SignUpState.Loading
            try {
                // 회원가입 API 호출
                Log.d("SignUpViewModel", "Starting registration for loginId: $loginId")
                val registerResponse = authService.register(
                    AuthRequest.RegisterRequest(
                        loginId = loginId,
                        password = password,
                        email = email,
                        nickname = nickname,
                        gender = gender,
                        birth = birth,
                        tagList = tagList
                    )
                )
                if (registerResponse.code == "SU") {
                    Log.d("SignUpViewModel", "Registration successful: ${registerResponse.message}")
                    Log.d("SignUpViewModel", "Proceeding to auto login for loginId: $loginId")
                    // 회원가입 성공 시 자동 로그인 API 호출
                    val request = AuthRequest.LoginRequest(loginId, password)
                    val response = authService.login(request).payload
                    if (response != null) {
                        Log.d("SignUpViewModel", "Login successful")
                        Log.d("SignUpViewModel", "AccessToken: ${response.accessToken}")
                        Log.d("SignUpViewModel", "RefreshToken: ${response.refreshToken}")
                        tokenManager.saveTokens(response.accessToken, response.refreshToken)
                        val user = UserEntity(
                            memberId = response.memberId,
                            profileImage = response.profileImage,
                            nickname = response.nickname
                        )
                        Log.d("SignUpViewModel", "Saving user to repository: $user")
                        userRepository.clearUser()
                        userRepository.saveUser(user) // 사용자 정보를 DB에 저장
                        val retrievedUser = userRepository.getUser()
                        Log.d("SignUpViewModel", "Retrieved user from repository: $retrievedUser")
                        _userInfo.value = retrievedUser // 사용자 정보를 StateFlow에 저장
                        _signUpState.value = SignUpState.Success("회원가입 및 로그인에 성공했습니다.")
                        onSuccess()
                    } else {
                        Log.e("SignUpViewModel", "Auto login failed")
                        _signUpState.value = SignUpState.Error("자동 로그인 실패")
                    }
                } else {
                    Log.e("SignUpViewModel", "Registration failed: code=${registerResponse.code}, message=${registerResponse.message}")
                    _signUpState.value = SignUpState.Error(registerResponse.message ?: "회원가입 실패")
                }
            } catch (e: Exception) {
                Log.e("SignUpViewModel", "Exception in registerAndAutoLogin: ${e.message}", e)
                _signUpState.value = SignUpState.Error("예외 발생: ${e.message}")
            }
        }
    }
}
