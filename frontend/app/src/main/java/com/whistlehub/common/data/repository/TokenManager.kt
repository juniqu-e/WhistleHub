//package com.whistlehub.common.data.repository
//
//import android.content.SharedPreferences
//import javax.inject.Inject
//import javax.inject.Singleton
//
//@Singleton
//class TokenManager @Inject constructor(
//    private val sharedPreferences: SharedPreferences
//) {
//    companion object {
//        private const val ACCESS_TOKEN_KEY = "access_token"
//        private const val REFRESH_TOKEN_KEY = "refresh_token"
//        private const val TOKEN_EXPIRY_KEY = "token_expiry"
//    }
//
//    fun saveTokens(accessToken: String, refreshToken: String, expiresIn: Long) {
//        sharedPreferences.edit()
//            .putString(ACCESS_TOKEN_KEY, accessToken)
//            .putString(REFRESH_TOKEN_KEY, refreshToken)
//            .putLong(TOKEN_EXPIRY_KEY, System.currentTimeMillis() + expiresIn * 1000)
//            .apply()
//    }
//
//    fun getAccessToken(): String? {
//        return sharedPreferences.getString(ACCESS_TOKEN_KEY, null)
//    }
//
//    fun getRefreshToken(): String? {
//        return sharedPreferences.getString(REFRESH_TOKEN_KEY, null)
//    }
//
//    fun clearTokens() {
//        sharedPreferences.edit()
//            .remove(ACCESS_TOKEN_KEY)
//            .remove(REFRESH_TOKEN_KEY)
//            .remove(TOKEN_EXPIRY_KEY)
//            .apply()
//    }
//
//    fun isTokenExpired(): Boolean {
//        val expiryTime = sharedPreferences.getLong(TOKEN_EXPIRY_KEY, 0)
//        return System.currentTimeMillis() > expiryTime
//    }
//}