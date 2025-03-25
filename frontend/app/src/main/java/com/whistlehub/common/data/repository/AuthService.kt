package com.whistlehub.common.data.repository

import com.whistlehub.common.data.remote.api.AuthApi
import com.whistlehub.common.data.remote.dto.request.AuthRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.AuthResponse
import com.whistlehub.common.util.TokenRefresh
import javax.inject.Inject
import javax.inject.Singleton

/**
--------------------------------------
인증 관련 API 호출을 담당하는 서비스 클래스
--------------------------------------
 **/

@Singleton
class AuthService @Inject constructor(
    private val authApi: AuthApi,
    private val tokenRefresh: TokenRefresh? = null
) : ApiRepository() {
    // 회원 가입
    suspend fun register(
        request: AuthRequest.RegisterRequest
    ): ApiResponse<AuthResponse.RegisterResponse> {
        return executeApiCall { authApi.register(request) }
    }
    // 아이디 중복 검사
    suspend fun checkDuplicateId(
        loginId: String
    ): ApiResponse<Boolean> {
        return executeApiCall { authApi.checkDuplicateId(loginId) }
    }
    // 닉네임 중복 검사
    suspend fun checkDuplicateNickname(
        nickname: String
    ): ApiResponse<Boolean> {
        return executeApiCall { authApi.checkDuplicateNickname(nickname) }
    }
    // 이메일 중복 검사
    suspend fun checkDuplicateEmail(
        email: String
    ): ApiResponse<Boolean> {
        return executeApiCall { authApi.checkDuplicateEmail(email) }
    }
    // 이메일 인증
    suspend fun sendEmailVerification(
        email: String
    ): ApiResponse<Unit> {
        return executeApiCall { authApi.sendEmailVerification(email) }
    }
    // 이메일 인증 코드 확인
    suspend fun validateEmailCode(
        request: AuthRequest.ValidateEmailRequest
    ): ApiResponse<Boolean> {
        return executeApiCall { authApi.validateEmailCode(request) }
    }
    // 비밀번호 초기화
    suspend fun resetPassword(
        request: AuthRequest.ResetPasswordRequest
    ): ApiResponse<Unit> {
        return executeApiCall { authApi.resetPassword(request) }
    }
    // 로그인
    suspend fun login(
        request: AuthRequest.LoginRequest
    ): ApiResponse<AuthResponse.LoginResponse> {
        return executeApiCall { authApi.login(request) }
    }
    // 토큰 갱신
    suspend fun updateToken(
        request: AuthRequest.UpdateTokenRequest
    ): ApiResponse<AuthResponse.UpdateTokenResponse> {
        return executeApiCall { authApi.updateToken(request) }
    }
}
