package com.whistlehub.common.util

import android.util.Log
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
 *
 * 주요 특징:
 * - 토큰 갱신 요청에 성공하면 새로운 액세스 토큰과 리프레시 토큰을 저장합니다.
 * - 토큰 갱신 요청에 실패하거나 예외가 발생하면, triggerLogout()을 호출하여
 *   tokenManager.clearTokens()와 함께 userRepository.clearUser()를 호출합니다.
 */

@Singleton
class TokenRefresh @Inject constructor(
    private val tokenManager: TokenManager,
    private val authService: AuthService,
    private val logoutManager: LogoutManager,
    private val userRepository: UserRepository
) : ApiRepository() {

    // 에러 코드 상수 (백엔드에서 정의한 코드)
    private companion object {
        const val CODE_TOKEN_EXPIRED = "EAT" // Expired Access Token
        const val CODE_TOKEN_INVALID = "IAT" // Invalid Access Token
        const val CODE_PERMISSION_DENIED = "NP" // Not Permitted 권한 없음 시
    }

    // 로그아웃 이벤트를 위한 Flow
    private val _logoutEventFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val logoutEventFlow: SharedFlow<Unit> = _logoutEventFlow

    // 액세스 토큰 만료 시 리프레시 토큰을 호출하는데 요청을 재시도하는 것을 막기 위한 변수
    private val permissionRetryMap = mutableMapOf<String, Boolean>()

    // 모든 API 호출에 대한 토큰 갱신 처리
    override suspend fun <T> executeApiCall(call: suspend () -> Response<ApiResponse<T>>): ApiResponse<T> {
        try {
            // API 호출 시도
            val response = super.executeApiCall(call)

            // 응답이 액세스 토큰 만료 인지 확인
            if (isTokenExpiredError(response)) {
                // 만료 되었다면 토큰 갱신 시도
                val refreshSuccess = refreshToken()

                // 토큰 갱신에 성공한 경우 API 재시도
                if (refreshSuccess) {
                    return super.executeApiCall(call)
                }
                // 토큰 갱신 실패 시 로그아웃 처리
                triggerLogout()
                return response
            }

            // 잘못된 토큰인지 확인
            if (isTokenInvalidError(response)) {
                // 만료 되었다면 토큰 갱신 시도
                val refreshSuccess = refreshToken()

                // 토큰 갱신에 성공한 경우 API 재시도
                if (refreshSuccess) {
                    return super.executeApiCall(call)
                }
                // 토큰 갱신 실패 시 로그아웃 처리
                triggerLogout()
                return response
            }


            // 응답이 권한 없음 인지 확인
            if (isPermissionDeniedError(response)) {
                // 요청의 고유 식별자 생성 -> 재시도 여부 확인을 위한 키
                val requestId = System.nanoTime().toString()

                // 이전에 재시도하지 않은 요청인 경우 한 번 더 시도
                if (permissionRetryMap[requestId] != true) {
                    permissionRetryMap[requestId] = true

                    // 재시도 요청
                    val retryResponse = super.executeApiCall(call)

                    // 재시도 후 식별자 제거
                    permissionRetryMap.remove(requestId)

                    // 재시도 후에도 권한 없음이면 로그아웃 처리
                    if (isPermissionDeniedError(retryResponse)) {
                        triggerLogout()
                    }

                    return retryResponse
                } else {
                    // 이미 재시도한 경우 로그아웃
                    triggerLogout()
                }
            }
            // 토큰 갱신 후 응답 처리
            return response
        } catch (e: Exception) {
            // 예외 처리 로직
            Log.e("TokenRefresh", "Exception in executeApiCall: ${e.message}")
            throw e
        }
    }

    // 토큰 만료 에러(EAT)인지 확인하는 함수
    private fun <T> isTokenExpiredError(response: ApiResponse<T>): Boolean {
        Log.d("TokenRefresh", "isTokenExpiredError: returning true for testing purposes")
        return response.code == CODE_TOKEN_EXPIRED
    }

    // 잘못된 토큰(IAT)인지 확인하는 함수
    private fun <T> isTokenInvalidError(response: ApiResponse<T>): Boolean {
        Log.d("TokenRefresh", "isTokenExpiredError: returning true for testing purposes")
        return response.code == CODE_TOKEN_INVALID
    }

    // 권한 없음 에러(NP)인지 확인하는 함수
    private fun <T> isPermissionDeniedError(response: ApiResponse<T>): Boolean {
        return response.code == CODE_PERMISSION_DENIED
    }

    // 토큰 갱신을 통해 새로운 액세스 토큰을 받는 함수
    private fun refreshToken(): Boolean {
        // 리프레시 토큰 정의, 만료될 시 null 반환
        val refreshToken = tokenManager.getRefreshToken() ?: return false


        return runBlocking {
            try {
                // 리프레시 토큰을 담아 API 요청을 하기 위한 객체 생성
                val refreshRequest = AuthRequest.UpdateTokenRequest(refreshToken)
                // 토큰 갱신 API 호출
                val response = authService.updateToken(refreshRequest)

                // 요청이 성공 하고 응답이 존재할 때
                if (response.code == "SU" && response.payload != null) {
                    with(response.payload) {
                        // 새로운 액세스 토큰과 리프레시 토큰을 저장
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
            userRepository.clearUser()
            _logoutEventFlow.emit(Unit)
            // LogoutManager를 통해 전역 로그아웃 이벤트 전달
            logoutManager.emitLogout()
        }
    }
}