package com.whistlehub.common.data.repository

import com.whistlehub.common.data.remote.api.AuthApi
import com.whistlehub.common.data.remote.dto.request.AuthRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.AuthResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthService @Inject constructor(
    private val authApi: AuthApi
) : ApiRepository() {

    suspend fun register(
        request: AuthRequest.RegisterRequest
    ): ApiResponse<AuthResponse.ResgisterResponse> {
        return executeApiCall { authApi.register(request) }
    }

    suspend fun checkDuplicateId(
        id: String
    ): ApiResponse<Boolean> {
        return executeApiCall { authApi.checkDuplicateId(id) }
    }

    suspend fun checkDuplicateNickname(
        nickname: String
    ): ApiResponse<Boolean> {
        return executeApiCall { authApi.checkDuplicateNickname(nickname) }
    }

    suspend fun checkDuplicateEmail(
        email: String
    ): ApiResponse<Boolean> {
        return executeApiCall { authApi.checkDuplicateEmail(email) }
    }

    suspend fun sendEmailVerification(
        email: String
    ): ApiResponse<Unit> {
        return executeApiCall { authApi.sendEmailVerification(email) }
    }

    suspend fun validateEmailCode(
        request: AuthRequest.ValidateEmailRequest
    ): ApiResponse<Boolean> {
        return executeApiCall { authApi.validateEmailCode(request) }
    }

    suspend fun resetPassword(
        request: AuthRequest.ResetPasswordRequest
    ): ApiResponse<Unit> {
        return executeApiCall { authApi.resetPassword(request) }
    }

    suspend fun login(
        request: AuthRequest.LoginRequest
    ): ApiResponse<AuthResponse.LoginResponse> {
        return executeApiCall { authApi.login(request) }
    }

    suspend fun updateToken(
        request: AuthRequest.UpdateTokenRequest
    ): ApiResponse<AuthResponse.UpdateTokenResponse> {
        return executeApiCall { authApi.updateToken(request) }
    }
}