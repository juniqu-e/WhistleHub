package com.whistlehub.common.data.remote.dto.response

/**
---------------------
인증 관련 API 응답 DTO
---------------------
 **/

sealed class AuthResponse {
    // 회원가입
    data class RegisterResponse(
        val refreshToken: String,
        val accessToken: String
    )
    // 태그 목록
    data class TagResponse(
        val id: Int,
        val name: String
    )
    // 로그인
    data class LoginResponse(
        val memberId: Int,
        val refreshToken: String,
        val accessToken: String,
        val profileImage: String,
        val nickname: String
    )
    // 토큰 갱신
    data class UpdateTokenResponse(
        val refreshToken: String,
        val accessToken: String,
    )
}