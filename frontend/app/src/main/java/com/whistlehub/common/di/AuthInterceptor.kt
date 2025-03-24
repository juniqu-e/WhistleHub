package com.whistlehub.common.di

import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : Interceptor {

    companion object {
        private const val TOKEN_KEY = "access_token"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = sharedPreferences.getString(TOKEN_KEY, null)
        val request = chain.request().newBuilder()

        // 토큰이 있을 경우 요청 헤더에 추가
        if (!accessToken.isNullOrEmpty()) {
            request.addHeader("Authorization", "Bearer $accessToken")
        }

        return chain.proceed(request.build())
    }
}