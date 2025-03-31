package com.whistlehub.common.util

import android.util.Log
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.whistlehub.common.data.local.room.UserRepository
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

/**
 * TokenRefresh 클래스는 액세스 토큰 만료 시 리프레시 토큰을 사용해 토큰을 갱신하는 기능을 제공합니다.
 * 만약 리프레시 토큰 갱신에 실패하면, 로그아웃 처리를 진행하여 토큰뿐 아니라 로컬에 저장된 유저 정보도 삭제합니다.
 */
@Singleton
class TokenRefresh @Inject constructor(
    private val tokenManager: TokenManager,
    private val authService: AuthService,
    private val logoutManager: LogoutManager,
    private val userRepository: UserRepository
) : ApiRepository() {

    // 백엔드에서 정의한 에러 코드 상수
    private companion object {
        const val CODE_TOKEN_EXPIRED = "EAT" // 만료된 액세스 토큰
        const val CODE_TOKEN_INVALID = "IAT" // 잘못된 액세스 토큰
        const val CODE_PERMISSION_DENIED = "NP" // 권한 없음
        // 리프레시 토큰 관련 오류 (로그아웃 처리)
        const val CODE_REFRESH_ERROR_1 = "IRT"
        const val CODE_REFRESH_ERROR_2 = "ERT"
        const val CODE_SUCCESS = "SU" // 성공 코드
    }

    // 로그아웃 이벤트를 위한 Flow
    private val _logoutEventFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val logoutEventFlow: SharedFlow<Unit> = _logoutEventFlow

    // 요청 재시도 여부를 추적하기 위한 맵
    private val permissionRetryMap = mutableMapOf<String, Boolean>()

    // Public wrapper 메서드
    suspend fun <T> execute(call: suspend () -> Response<ApiResponse<T>>): ApiResponse<T> {
        return executeApiCall(call)
    }

    // 모든 API 호출에 대한 토큰 갱신 처리
    override suspend fun <T> executeApiCall(call: suspend () -> Response<ApiResponse<T>>): ApiResponse<T> {
        try {
            Log.d("TokenRefresh", "Executing API call...")
            val response = super.executeApiCall(call)
            Log.d("TokenRefresh", "Initial API response: code=${response.code}, body=$response")

            // 액세스 토큰 관련 에러 (EAT 또는 IAT) 처리
            if (isTokenExpiredError(response) || isTokenInvalidError(response)) {
                Log.d("TokenRefresh", "Detected token issue (code=${response.code}). Attempting token refresh...")
                val currentRefreshToken = tokenManager.getRefreshToken()
                if (currentRefreshToken == null) {
                    Log.e("TokenRefresh", "No refresh token available.")
                    return response
                }

                val refreshRequest = AuthRequest.UpdateTokenRequest(currentRefreshToken)
                Log.d("TokenRefresh", "Calling refresh API with refreshToken: $currentRefreshToken")

                // AuthService가 ApiResponse를 직접 반환하는 경우
                val refreshApiResponse = authService.updateToken(refreshRequest)
                Log.d("TokenRefresh", "Refresh API response: code=${refreshApiResponse.code}, body=$refreshApiResponse")

                when (refreshApiResponse.code.toString()) {
                    CODE_SUCCESS -> {
                        if (refreshApiResponse.payload != null) {
                            with(refreshApiResponse.payload) {
                                Log.d("TokenRefresh", "Refresh successful: new accessToken=$accessToken, new refreshToken=$refreshToken")
                                tokenManager.saveTokens(this.accessToken, this.refreshToken)
                            }
                            Log.d("TokenRefresh", "Retrying original API call after token refresh...")
                            val retryResponse = super.executeApiCall(call)
                            Log.d("TokenRefresh", "Retry API response: code=${retryResponse.code}, body=$retryResponse")
                            return retryResponse
                        } else {
                            Log.e("TokenRefresh", "Refresh API returned SU but payload is null.")
                            tokenManager.clearTokens()
                            return response
                        }
                    }
                    CODE_REFRESH_ERROR_1, CODE_REFRESH_ERROR_2 -> {
                        Log.e("TokenRefresh", "Refresh failed with code ${refreshApiResponse.code}. Triggering logout.")
                        triggerLogout()
                        return response
                    }
                    else -> {
                        Log.e("TokenRefresh", "Unknown refresh response code: ${refreshApiResponse.code}")
                        tokenManager.clearTokens()
                        return response
                    }
                }
            }

            // 권한 없음 (NP) 처리
            if (isPermissionDeniedError(response)) {
                val requestId = System.nanoTime().toString()
                Log.d("TokenRefresh", "Permission denied (code=${response.code}). RequestId: $requestId. Retrying once...")
                if (permissionRetryMap[requestId] != true) {
                    permissionRetryMap[requestId] = true
                    val retryResponse = super.executeApiCall(call)
                    permissionRetryMap.remove(requestId)
                    if (isPermissionDeniedError(retryResponse)) {
                        Log.e("TokenRefresh", "Retry still permission denied. Triggering logout.")
                        triggerLogout()
                    }
                    Log.d("TokenRefresh", "Retry API response for permission denied: code=${retryResponse.code}")
                    return retryResponse
                } else {
                    Log.e("TokenRefresh", "Already retried permission denied request. Triggering logout.")
                    triggerLogout()
                }
            }

            Log.d("TokenRefresh", "API call completed successfully with code=${response.code}")
            return response
        } catch (e: Exception) {
            Log.e("TokenRefresh", "Exception in executeApiCall: ${e.message}")
            throw e
        }
    }

    // 에러 확인 함수들 (code가 Int일 경우 toString()으로 비교)
    private fun <T> isTokenExpiredError(response: ApiResponse<T>): Boolean {
        return response.code.toString() == CODE_TOKEN_EXPIRED
    }

    private fun <T> isTokenInvalidError(response: ApiResponse<T>): Boolean {
        return response.code.toString() == CODE_TOKEN_INVALID
    }

    private fun <T> isPermissionDeniedError(response: ApiResponse<T>): Boolean {
        return response.code.toString() == CODE_PERMISSION_DENIED
    }

    // 별도의 토큰 갱신 함수 (외부에서 직접 호출할 수 있음)
    fun refreshToken(): Boolean {
        val currentRefreshToken = tokenManager.getRefreshToken() ?: return false
        return runBlocking {
            try {
                val refreshRequest = AuthRequest.UpdateTokenRequest(currentRefreshToken)
                // AuthService가 ApiResponse를 직접 반환하는 경우
                val refreshApiResponse = authService.updateToken(refreshRequest)

                if (refreshApiResponse.code.toString() == CODE_SUCCESS && refreshApiResponse.payload != null) {
                    with(refreshApiResponse.payload) {
                        tokenManager.saveTokens(this.accessToken, this.refreshToken)
                    }
                    true
                } else {
                    if (refreshApiResponse.code.toString() in listOf(CODE_REFRESH_ERROR_1, CODE_REFRESH_ERROR_2)) {
                        triggerLogout()
                    }
                    tokenManager.clearTokens()
                    false
                }
            } catch (e: Exception) {
                Log.e("TokenRefresh", "Exception during token refresh: ${e.message}")
                tokenManager.clearTokens()
                false
            }
        }
    }

    // 로그아웃 이벤트 발생 함수
    private fun triggerLogout() {
        tokenManager.clearTokens()
        runBlocking {
            userRepository.clearUser()
            _logoutEventFlow.emit(Unit)
            logoutManager.emitLogout()
        }
    }
}