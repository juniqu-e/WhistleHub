package com.whistlehub.common.data.remote.dto.request

/**
---------------------
인증 관련 API 요청 DTO
---------------------
 **/

sealed class AuthRequest {
    // 회원가입
    data class RegisterRequest(
        val loginId: String,
        val password: String,
        val email: String,
        val nickname: String
    )
    // 이메일 인증 코드 확인
    data class ValidateEmailRequest(
        val email: String,
        val code: String
    )
    // 비밀번호 초기화
    data class ResetPasswordRequest(
        val email: String,
        val newPassword: String
    )
    // 로그인
    data class LoginRequest(
        val loginId: String,
        val password: String
    )
    // 토큰 갱신
    data class UpdateTokenRequest(
        val refreshToken: String
    )
}