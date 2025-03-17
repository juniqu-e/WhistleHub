package com.whistlehub.common.data.repository

import com.whistlehub.common.data.remote.api.AuthApi
import com.whistlehub.common.data.remote.dto.request.AuthRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.AuthResponse
import javax.inject.Inject

class AuthService @Inject constructor(
    private val authApi: AuthApi
) : ApiRepository() {

    suspend fun register(registerRequest: AuthRequest.Register): ApiResponse<AuthResponse.ResgisterResponse> {
        return executeApiCall { authApi.register(registerRequest) }
    }

    suspend fun checkDuplicateId(id: String): ApiResponse<Boolean> {
        return executeApiCall { authApi.checkDuplicateId(id) }
    }

    suspend fun checkDuplicateNickname(nickname: String): ApiResponse<Boolean> {
        return executeApiCall { authApi.checkDuplicateNickname(nickname) }
    }

    suspend fun checkDuplicateEmail(email: String): ApiResponse<Boolean> {
        return executeApiCall { authApi.checkDuplicateEmail(email) }
    }

    suspend fun sendEmailVerification(email: String): ApiResponse<Unit> {
        return executeApiCall { authApi.sendEmailVerification(email) }
    }

    suspend fun validateEmailCode(validateRequest: AuthRequest.ValidateEmail): ApiResponse<Boolean> {
        return executeApiCall { authApi.validateEmailCode(validateRequest) }
    }

    suspend fun resetPassword(resetRequest: AuthRequest.ResetPassword): ApiResponse<Unit> {
        return executeApiCall { authApi.resetPassword(resetRequest) }
    }

    suspend fun login(loginRequest: AuthRequest.Login): ApiResponse<AuthResponse.LoginResponse> {
        return executeApiCall { authApi.login(loginRequest) }
    }

    suspend fun refreshToken(refreshRequest: AuthRequest.RefreshToken): ApiResponse<AuthResponse.Token> {
        return executeApiCall { authApi.refreshToken(refreshRequest) }
    }
}