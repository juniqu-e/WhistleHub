package com.whistlehub.common.data.remote.api

import com.whistlehub.common.data.remote.dto.request.ProfileRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.ProfileResponse
import com.whistlehub.common.view.navigation.Screen
import retrofit2.Response
import retrofit2.http.*

/**
-----------------------
프로필 관련 API 인터페이스
-----------------------
 **/

interface ProfileApi {
    @GET("member")
    suspend fun getProfile(
        @Header("Authorization") token: String,
        @Query("memberId") memberId: Int
    ): Response<ApiResponse<ProfileResponse.GetProfileResponse>>

    @PUT("member")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: ProfileRequest.UpdateProfileRequest
    ): Response<ApiResponse<Unit>>

    @DELETE("member")
    suspend fun deleteProfile(
        @Header("Authorization") token: String
    ): Response<ApiResponse<Unit>>

    @POST("member/image")
    suspend fun uploadProfileImage(
        @Header("Authrization") token: String,
        @Body request: ProfileRequest.UploadProfileImageRequest
    ): Response<ApiResponse<String>>

    @PUT("member/password")
    suspend fun changePassword(
        @Header("Authrization") token: String,
        @Body request: ProfileRequest.ChangePasswordRequest
    ): Response<ApiResponse<Unit>>

//    @GET("member/search")
//    suspend fun searchProfile(
//        @Header("Authorization") token: String,
//        @Query("memberId") memberId: Int
//    ): Response<ApiResponse<ProfileResponse.GetProfileResponse>>

    @GET("member/playlist")
    suspend fun getMemberPlaylists(
        @Header("Authorization") token: String,
        @Query("memberId") memberId: Int? = null
    ): Response<ApiResponse<ProfileResponse.GetMemberPlaylistsResponse>>

    @GET("member/playlist")
    suspend fun getMemberTracks(
        @Header("Authorization") token: String,
        @Query("memberId") memberId: Int? = null,
        @Query("page") page: Int,
        @Query("orderby") orderby: String
    ): Response<ApiResponse<ProfileResponse.GetMemberTracksResponse>>

    @POST("member/follow")
    suspend fun follow(
        @Header("Authorization") token: String,
        @Body request: ProfileRequest.FollowRequest
    ): Response<ApiResponse<Unit>>

    @GET("member/follower")
    suspend fun getFollowers(
        @Header("Authorization") token: String,
        @Query("memberId") memberId: Int? = null,
        @Query("page") page: Int
    ): Response<ApiResponse<ProfileResponse.GetFollowersResponse>>

    @GET("member/following")
    suspend fun getFollowings(
        @Header("Authorization") token: String,
        @Query("memberId") memberId: Int? = null,
        @Query("page") page: Int
    ): Response<ApiResponse<ProfileResponse.GetFollowingsResponse>>
}