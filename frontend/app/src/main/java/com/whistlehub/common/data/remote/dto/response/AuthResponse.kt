package com.whistlehub.common.data.remote.dto.response

/**
-----------------------
인증 관련 API 응답 DTO
-----------------------
 **/

sealed class AuthResponse {
    data class ResgisterResponse(
        val refreshToken: String,
        val accessToken: String
    )

    data class LoginResponse(
        val refreshToken: String,
        val accessToken: String,
        val profileImage: String,
        val nickname: String
    )

    data class UpdateTokenResponse(
        val refreshToken: String,
        val accessToken: String,
    )
}