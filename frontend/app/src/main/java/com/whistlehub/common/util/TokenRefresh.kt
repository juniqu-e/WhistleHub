package com.whistlehub.common.util

import com.whistlehub.common.data.remote.dto.request.AuthRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.repository.ApiRepository
import com.whistlehub.common.data.repository.AuthService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.runBlocking
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRefresh @Inject constructor(
    private val tokenManager: TokenManager,
    private val authService: AuthService
) : ApiRepository() {

    // 에러 코드 상수
    private companion object {
        const val CODE_TOKEN_EXPIRED = "AF" // Access token 만료
        const val CODE_PERMISSION_DENIED = "NP" // 권한 없음
    }

    // 로그아웃 이벤트를 위한 Flow
    private val _logoutEventFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val logoutEventFlow: SharedFlow<Unit> = _logoutEventFlow

    // 권한 없음 재시도 추적을 위한 맵
    private val permissionRetryMap = mutableMapOf<String, Boolean>()

    override suspend fun <T> executeApiCall(call: suspend () -> Response<ApiResponse<T>>): ApiResponse<T> {
        try {
            // API 호출 시도
            val response = super.executeApiCall(call)

            // 토큰 만료 에러 처리
            if (isTokenExpiredError(response)) {
                // 토큰 갱신 시도
                val refreshSuccess = refreshToken()

                // 토큰 갱신에 성공한 경우 API 재시도
                if (refreshSuccess) {
                    return super.executeApiCall(call)
                }
                // 토큰 갱신 실패 시 로그아웃 처리
                triggerLogout()
                return response
            }

            // 권한 없음 에러 처리
            if (isPermissionDeniedError(response)) {
                // 요청의 고유 식별자 생성
                val requestId = System.nanoTime().toString()

                // 이전에 재시도하지 않은 요청인 경우 한 번 더 시도
                if (permissionRetryMap[requestId] != true) {
                    permissionRetryMap[requestId] = true

                    // 재시도 요청
                    val retryResponse = super.executeApiCall(call)

                    // 재시도 후 맵에서 제거
                    permissionRetryMap.remove(requestId)

                    // 재시도 결과 확인
                    if (isPermissionDeniedError(retryResponse)) {
                        // 다시 권한 없음이면 로그아웃 처리
                        triggerLogout()
                    }

                    return retryResponse
                } else {
                    // 이미 재시도한 경우 로그아웃
                    triggerLogout()
                }
            }

            return response
        } catch (e: Exception) {
            // 예외 처리 로직
            throw e
        }
    }

    // 토큰 만료 에러인지 확인하는 함수
    private fun <T> isTokenExpiredError(response: ApiResponse<T>): Boolean {
        // 백엔드 정의에 따라 토큰 만료 에러 확인: code "AF"
        return response.code == CODE_TOKEN_EXPIRED
    }

    // 권한 없음 에러인지 확인하는 함수
    private fun <T> isPermissionDeniedError(response: ApiResponse<T>): Boolean {
        // 백엔드 정의에 따라 권한 없음 에러 확인: code "NP"
        return response.code == CODE_PERMISSION_DENIED
    }

    private fun refreshToken(): Boolean {
        val refreshToken = tokenManager.getRefreshToken() ?: return false

        return runBlocking {
            try {
                val refreshRequest = AuthRequest.UpdateTokenRequest(refreshToken)
                val response = authService.updateToken(refreshRequest)

                if (response.code == "SU" && response.payload != null) {
                    with(response.payload) {
                        tokenManager.saveTokens(accessToken, refreshToken)
                    }
                    true // 토큰 갱신 성공
                } else {
                    // 토큰 갱신 실패 시 로그아웃 처리
                    tokenManager.clearTokens()
                    false // 토큰 갱신 실패
                }
            } catch (e: Exception) {
                // 예외 발생 시 토큰 갱신 실패로 처리
                tokenManager.clearTokens()
                false
            }
        }
    }

    // 로그아웃 이벤트 발생 함수
    private fun triggerLogout() {
        tokenManager.clearTokens()
        runBlocking {
            _logoutEventFlow.emit(Unit)
        }
    }
}