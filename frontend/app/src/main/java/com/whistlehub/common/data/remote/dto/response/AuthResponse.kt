package com.whistlehub.common.data.remote.dto.response

import com.whistlehub.common.data.remote.dto.request.AuthRequest

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

    data class Token(
        val accessToken: String,
        val refreshToken: String,
        val expiresIn: Long
    )
}