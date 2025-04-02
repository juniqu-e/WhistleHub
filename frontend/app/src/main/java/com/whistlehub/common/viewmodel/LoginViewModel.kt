package com.whistlehub.common.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whistlehub.common.data.local.entity.UserEntity
import com.whistlehub.common.data.local.room.UserRepository
import com.whistlehub.common.data.remote.dto.request.AuthRequest
import com.whistlehub.common.data.repository.AuthService
import com.whistlehub.common.util.TokenManager
import com.whistlehub.common.util.TokenRefresh
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject



/**
 * LoginState는 로그인 과정의 상태를 나타내기 위한 sealed class 입니다.
 * 각 상태는 다음과 같이 정의됩니다:
 * - Idle: 초기 상태, 아무런 작업도 수행하지 않는 상태
 * - Loading: 로그인 요청 중 (로딩중)인 상태
 * - Success: 로그인 성공 상태
 * - Error: 로그인 실패 또는 오류 발생 시 에러 메시지와 함께 전달되는 상태
 */
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

/**
 * LoginViewModel은 사용자 로그인 및 로그아웃과 관련된 비즈니스 로직을 처리합니다.
 *
 * 주요 기능:
 * - 로그인 API 호출을 통해 사용자 인증 및 토큰 저장 처리
 * - 로그인 성공 시 로컬 데이터베이스(UserRepository)에 사용자 정보를 저장하고, StateFlow를 통해 UI에 반영
 * - 로그아웃 시 토큰과 로컬 사용자 정보를 삭제하며, 로그아웃 이벤트를 SharedFlow를 통해 발행하여 UI가 이를 구독하도록 함
 *
 * Hilt를 이용해 의존성 주입받으며, 앱 전역에서 하나의 인스턴스로 사용됩니다.
 */

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authService: AuthService,
    private val tokenManager: TokenManager,
    private val userRepository: UserRepository
) : ViewModel() {
    // 토큰 만료 시 로그아웃 이벤트를 받을 수 있는 Flow
    private val logoutEventFlowInternal = MutableSharedFlow<Unit>()
    val logoutEventFlow: SharedFlow<Unit> = logoutEventFlowInternal
    // 로그인 상태를 확인하는 StateFlow
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState
    // 사용자 정보를 저장하는 StateFlow
    private val _userInfo = MutableStateFlow<UserEntity?>(null)
    val userInfo: StateFlow<UserEntity?> = _userInfo

    /**
     * 로그인 함수: 사용자가 로그인 버튼을 클릭하면 호출됩니다.
     *
     * 처리 과정:
     * 1. 입력값(아이디와 비밀번호)의 유효성 검사: 비어있는 경우 에러 상태를 업데이트하고 종료
     * 2. 상태를 Loading으로 변경한 후, viewModelScope 내에서 API 호출
     * 3. AuthService를 통해 로그인 API 호출을 수행하고, 응답의 payload가 null이 아닌 경우:
     *    - TokenManager를 사용해 액세스 토큰과 리프레시 토큰 저장
     *    - 응답으로 받은 사용자 정보를 UserEntity로 생성하여 UserRepository에 저장
     *    - _userInfo를 업데이트하여 UI에 사용자 정보 반영
     *    - _loginState를 Success로 변경
     * 4. 응답이 null이거나 예외 발생 시, _loginState를 Error 상태로 업데이트
     */
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
                val response = authService.login(request).payload
                if (response != null) {
                    tokenManager.saveTokens(response.accessToken, response.refreshToken)
                    val user = UserEntity(
                        memberId = response.memberId,
                        profileImage = response.profileImage,
                        nickname = response.nickname
                    )
                    userRepository.clearUser()
                    userRepository.saveUser(user) // 사용자 정보를 DB에 저장
                    _userInfo.value = userRepository.getUser() // 사용자 정보를 StateFlow에 저장
                    _loginState.value = LoginState.Success
                } else {
                    _loginState.value = LoginState.Error("로그인 실패")
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
