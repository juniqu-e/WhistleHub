package com.whistlehub.common.data.remote.api

import com.whistlehub.common.data.remote.dto.request.ProfileRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.ProfileResponse
import retrofit2.http.*

interface ProfileApi {



    @GET("member/following")
    suspend fun getFollowing(
        @Query("memberId") memberId: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): ApiResponse<List<ProfileResponse.Follow>>

    @GET("member/follower")
    suspend fun getFollowers(
        @Query("memberId") memberId: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): ApiResponse<List<ProfileResponse.Follow>>

    @POST("member/follow")
    suspend fun toggleFollow(@Body request: ProfileRequest.Follow): ApiResponse<Boolean>

}