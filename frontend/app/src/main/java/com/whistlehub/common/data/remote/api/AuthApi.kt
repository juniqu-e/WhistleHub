package com.whistlehub.common.data.remote.api

import com.whistlehub.common.data.remote.dto.request.AuthRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.AuthResponse
import retrofit2.Response
import retrofit2.http.*

/**
-----------------------
인증 관련 API 인터페이스
-----------------------
 **/

interface AuthApi {
    // 회원 가입
    @POST("auth/register")
    suspend fun register(
        @Body request: AuthRequest.RegisterRequest
    ): Response<ApiResponse<Int>>
    // 태그 목록 요청
    @GET("auth/tag")
    suspend fun getTagList(
    ): Response<ApiResponse<List<AuthResponse.TagResponse>>>
    // 아이디 중복 검사
    @GET("auth/duplicated/id")
    suspend fun checkDuplicateId(
        @Query("loginId") loginId: String
    ): Response<ApiResponse<Boolean>>
    // 닉네임 중복 검사
    @GET("auth/duplicated/nickname")
    suspend fun checkDuplicateNickname(
        @Query("nickname") nickname: String
    ): Response<ApiResponse<Boolean>>
    // 이메일 중복 검사
    @GET("auth/duplicated/email")
    suspend fun checkDuplicateEmail(
        @Query("email") email: String
    ): Response<ApiResponse<Boolean>>
    // 이메일 인증
    @GET("auth/email")
    suspend fun sendEmailVerification(
        @Query("email") email: String
    ): Response<ApiResponse<Unit>>
    // 이메일 인증 코드 확인
    @POST("auth/validate/email")
    suspend fun validateEmailCode(
        @Body request: AuthRequest.ValidateEmailRequest
    ): Response<ApiResponse<Boolean>>
    // 비밀번호 초기화
    @POST("auth/reset/password")
    suspend fun resetPassword(
        @Body request: AuthRequest.ResetPasswordRequest
    ): Response<ApiResponse<Unit>>
    // 로그인
    @POST("auth/login")
    suspend fun login(
        @Body request: AuthRequest.LoginRequest
    ): Response<ApiResponse<AuthResponse.LoginResponse>>
    // 토큰 갱신
    @POST("auth/refresh")
    suspend fun updateToken(
        @Body request: AuthRequest.UpdateTokenRequest
    ): Response<ApiResponse<AuthResponse.UpdateTokenResponse>>
}