package com.whistlehub.common.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whistlehub.common.data.local.room.UserRepository
import com.whistlehub.common.data.remote.dto.request.AuthRequest
import com.whistlehub.common.data.repository.AuthService
import com.whistlehub.common.util.TokenManager
import com.whistlehub.common.util.TokenRefresh
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginSplashViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val tokenRefresh: TokenRefresh,
    private val authService: AuthService,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> get() = _isLoggedIn

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            try {
                val refreshToken = tokenManager.getRefreshToken()

                // 리프레시 토큰이 없으면 로그인 필요
                if (refreshToken.isNullOrEmpty()) {
                    Log.d("LoginSplashViewModel", "리프레시 토큰 없음, 로그인 필요")
                    _isLoggedIn.value = false
                    return@launch
                }

                // 리프레시 토큰이 있으면 유효성 확인 및 액세스 토큰 갱신
                val request = AuthRequest.UpdateTokenRequest(refreshToken)
                val response = authService.updateToken(request)

                if (response.code == "SU" && response.payload != null) {
                    // 토큰 갱신 성공
                    Log.d("LoginSplashViewModel", "토큰 갱신 성공")

                    // 새 토큰 저장
                    tokenManager.saveTokens(
                        response.payload.accessToken,
                        response.payload.refreshToken
                    )

                    // 사용자 정보가 로컬에 없으면 가져오기
                    if (userRepository.getUser() == null) {
                        Log.d("LoginSplashViewModel", "로컬 사용자 정보 없음, 프로필 로드 필요")
                        // 여기서 필요하다면 사용자 프로필 정보를 가져와 저장하는 로직 추가
                    }

                    _isLoggedIn.value = true
                } else {
                    // 토큰 갱신 실패 (만료 또는 유효하지 않은 토큰)
                    Log.d("LoginSplashViewModel", "토큰 갱신 실패: ${response.message}")
                    tokenManager.clearTokens() // 토큰 삭제
                    userRepository.clearUser() // 사용자 정보 삭제
                    _isLoggedIn.value = false
                }
            } catch (e: Exception) {
                // 네트워크 오류 등 예외 발생
                Log.e("LoginSplashViewModel", "자동 로그인 중 오류 발생", e)
                _isLoggedIn.value = false
            }
        }
    }

    // 수동으로 로그인 상태 확인 (재시도용)
    fun retryLoginCheck() {
        _isLoggedIn.value = null // 로딩 상태로 변경
        checkLoginStatus()
    }
}