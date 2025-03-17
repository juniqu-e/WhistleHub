package com.whistlehub.common.data.repository

import com.whistlehub.common.data.remote.dto.request.AuthRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import kotlinx.coroutines.runBlocking
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
abstract class TokenRefresh @Inject constructor(
    private val tokenManager: TokenManager,
    private val authService: AuthService
) : ApiRepository() {

    override suspend fun <T> executeApiCall(call: suspend () -> Response<ApiResponse<T>>): ApiResponse<T> {
        // 토큰이 만료되었으면 갱신 시도
        if (tokenManager.isTokenExpired()) {
            refreshToken()
        }

        return super.executeApiCall(call)
    }

    private fun refreshToken() {
        val refreshToken = tokenManager.getRefreshToken() ?: return

        runBlocking {
            val refreshRequest = AuthRequest.RefreshToken(refreshToken)
            val response = authService.refreshToken(refreshRequest)

            if (response.code == "SU" && response.payload != null) {
                with(response.payload) {
                    tokenManager.saveTokens(accessToken, refreshToken, expiresIn)
                }
            } else {
                // 토큰 갱신 실패 시 로그아웃 처리
                tokenManager.clearTokens()
                // 로그아웃 이벤트 발생 처리 (EventBus나 Flow 등을 사용할 수 있음)
            }
        }
    }
}