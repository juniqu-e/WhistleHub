package com.whistlehub.common.di

import com.whistlehub.common.util.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 AuthInterceptor 파일은 API 요청 시 토큰을 헤더에 추가하는 역할을 담당합니다.
 Interceptor: OkHttp에서 제공하는 인터셉터 인터페이스로 네트워크 요청의 중간에 수정할 수 있는 기능
 Chain: 현재 요청에 대한 정보를 가지고 있는 객체
 **/

@Singleton
class AuthInterceptor @Inject constructor(
    // 저장된 토큰 관리
    private val tokenManager: TokenManager
) : Interceptor {
    // 상위 클래스인 Intercepor의 추상 메서드인 intercept를 구현
    override fun intercept(chain: Interceptor.Chain): Response {
        // 저장된 액세스 토큰 가져오기
        val accessToken = tokenManager.getAccessToken()
        val request = chain.request().newBuilder()

        // 액세스 토큰이 존재하면 요청 헤더에 "Authorization" 키로 추가, 값은 "Bearer 액세스토큰" 형태로 추가
        if (!accessToken.isNullOrEmpty()) {
            request.addHeader("Authorization", "Bearer $accessToken")
        }
        // 변경된 요청 실행
        return chain.proceed(request.build())
    }
}