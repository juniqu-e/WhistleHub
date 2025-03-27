package com.whistlehub.common.util

import android.content.SharedPreferences
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 Token Manager 파일은 액세스 토큰과 리프레시 토큰을 저장하고 관리하는 클래스입니다.
 SharedPreferences:
 - Android에서 제공하는 경량 데이터 저장소
 - 인증 토큰 등의 작은 데이터를 키-값 쌍으로 저장하고 불러올 수 있게 해주는 역할
 - 데이터는 앱이 종료된 후에도 유지되며 파일 시스템에 XML 형식으로 저장
 - 장점 : 빠르고 간편하게 접근할 수 있습니다.
 **/

@Singleton
class TokenManager @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    companion object {
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
    }

    // 액세스 토큰과 리프레시 토큰을 SharedPreferences에 저장
    fun saveTokens(accessToken: String, refreshToken: String) {
        sharedPreferences.edit()
            .putString(ACCESS_TOKEN_KEY, accessToken)
            .putString(REFRESH_TOKEN_KEY, refreshToken)
            .apply()
        Log.d("TokenManager", "AccessToken: $accessToken, RefreshToken: $refreshToken saved")
    }

    // 액세스 토큰을 SharedPreferences에서 가져오기
    fun getAccessToken(): String? {
        return sharedPreferences.getString(ACCESS_TOKEN_KEY, null)
    }

    // 리프레시 토큰을 SharedPreferences에서 가져오기
    fun getRefreshToken(): String? {
        return sharedPreferences.getString(REFRESH_TOKEN_KEY, null)
    }

    // SharedPreferences에 저장된 토큰 삭제 -> 로그아웃 처리
    fun clearTokens() {
        sharedPreferences.edit()
            .remove(ACCESS_TOKEN_KEY)
            .remove(REFRESH_TOKEN_KEY)
            .apply() // 변경사항 비동기 적용
    }
}