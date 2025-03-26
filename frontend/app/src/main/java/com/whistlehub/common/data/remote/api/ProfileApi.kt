package com.whistlehub.common.data.remote.api

import com.whistlehub.common.data.remote.dto.request.ProfileRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.ProfileResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
-----------------------
프로필 관련 API 인터페이스
-----------------------
 **/

interface ProfileApi {
    // 프로필 조회 (memberId가 생략 된 경우 AccessToken의 memberId 조회)
    @GET("member")
    suspend fun getProfile(
        @Query("memberId") memberId: Int?
    ): Response<ApiResponse<ProfileResponse.GetProfileResponse>>
    // 프로필 정보 수정
    @PUT("member")
    suspend fun updateProfile(
        @Body request: ProfileRequest.UpdateProfileRequest
    ): Response<ApiResponse<Unit>>
    // 회원 탈퇴
    @DELETE("member")
    suspend fun deleteProfile(
    ): Response<ApiResponse<Unit>>
    // 프로필 사진 업로드 (response : 업로드 된 이미지 링크)
    @Multipart
    @POST("member/image")
    suspend fun uploadProfileImage(
        @Part("memberId") memberId: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<ApiResponse<String>>
    // 비밀번호 변경
    @PUT("member/password")
    suspend fun changePassword(
        @Body request: ProfileRequest.ChangePasswordRequest
    ): Response<ApiResponse<Unit>>
    // 멤버 검색
    @GET("member/search")
    suspend fun searchProfile(
        @Query("query") query: String, // 검색어
        @Query("page") page: Int, // 페이지
        @Query("size") size: Int, // 페이지 크기
        @Query("orderby") orderby: String // 정렬 기준
    ): Response<ApiResponse<List<ProfileResponse.SearchProfileResponse>>>
    // 멤버의 플레이리스트 조회 (memberId가 생략 된 경우 AccessToken의 memberId 조회)
    @GET("member/playlist")
    suspend fun getMemberPlaylists(
        @Query("memberId") memberId: Int? = null
    ): Response<ApiResponse<List<ProfileResponse.GetMemberPlaylistsResponse>>>
    // 멤버의 트랙 조회 (memberId가 생략 된 경우 AccessToken의 memberId 조회)
    @GET("member/track")
    suspend fun getMemberTracks(
        @Query("memberId") memberId: Int? = null,
        @Query("page") page: Int,
        @Query("orderby") orderby: String
    ): Response<ApiResponse<List<ProfileResponse.GetMemberTracksResponse>>>
    // 팔로우
    @POST("member/follow")
    suspend fun follow(
        @Body request: ProfileRequest.FollowRequest
    ): Response<ApiResponse<Unit>>
    // 멤버의 팔로워 목록 조회 (memberId가 생략 된 경우 AccessToken의 memberId 조회)
    @GET("member/follower")
    suspend fun getFollowers(
        @Query("memberId") memberId: Int? = null,
        @Query("page") page: Int
    ): Response<ApiResponse<List<ProfileResponse.GetFollowersResponse>>>
    // 멤버의 팔로잉 목록 조회 (memberId가 생략 된 경우 AccessToken의 memberId 조회)
    @GET("member/following")
    suspend fun getFollowings(
        @Query("memberId") memberId: Int? = null,
        @Query("page") page: Int
    ): Response<ApiResponse<List<ProfileResponse.GetFollowingsResponse>>>
}