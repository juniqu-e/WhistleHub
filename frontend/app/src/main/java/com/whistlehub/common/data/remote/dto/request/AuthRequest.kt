package com.whistlehub.common.data.remote.dto.request

sealed class AuthRequest {
    data class Register(
        val loginId: String,
        val password: String,
        val email: String,
        val nickname: String
    )

    data class Login(
        val loginId: String,
        val password: String
    )

    data class ValidateEmail(
        val email: String,
        val code: String
    )

    data class ResetPassword(
        val email: String,
        val newPassword: String
    )

    data class RefreshToken(
        val refreshToken: String
    )
}