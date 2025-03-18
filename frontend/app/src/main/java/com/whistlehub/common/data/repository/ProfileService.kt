package com.whistlehub.common.data.repository

import com.whistlehub.common.data.remote.api.ProfileApi
import com.whistlehub.common.data.remote.dto.request.ProfileRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.ProfileResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileService @Inject constructor(
    private val profileApi: ProfileApi
) : ApiRepository() {

    suspend fun getProfile(
        token: String,
        memberId: Int
    ): ApiResponse<ProfileResponse.GetProfileResponse> {
        return executeApiCall { profileApi.getProfile(token, memberId) }
    }

    suspend fun updateProfile(
        token: String,
        request: ProfileRequest.UpdateProfileRequest
    ): ApiResponse<Unit> {
        return executeApiCall { profileApi.updateProfile(token, request) }
    }

    suspend fun deleteProfile(
        token: String
    ): ApiResponse<Unit> {
        return executeApiCall { profileApi.deleteProfile(token) }
    }

    suspend fun uploadProfileImage(
        token: String,
        request: ProfileRequest.UploadProfileImageRequest
    ): ApiResponse<String> {
        return executeApiCall { profileApi.uploadProfileImage(token, request) }
    }

    suspend fun changePassword(
        token: String,
        request: ProfileRequest.ChangePasswordRequest
    ): ApiResponse<Unit> {
        return executeApiCall { profileApi.changePassword(token, request) }
    }

    suspend fun getMemberPlaylists(
        token: String,
        memberId: Int? = null
    ): ApiResponse<List<ProfileResponse.GetMemberPlaylistsResponse>> {
        return executeApiCall { profileApi.getMemberPlaylists(token, memberId) }
    }

    suspend fun getMemberTracks(
        token: String,
        memberId: Int? = null,
        page: Int,
        orderby: String
    ): ApiResponse<List<ProfileResponse.GetMemberTracksResponse>> {
        return executeApiCall { profileApi.getMemberTracks(token, memberId, page, orderby) }
    }

    suspend fun follow(
        token: String,
        request: ProfileRequest.FollowRequest
    ): ApiResponse<Unit> {
        return executeApiCall { profileApi.follow(token, request) }
    }

    suspend fun getFollowers(
        token: String,
        memberId: Int? = null,
        page: Int
    ): ApiResponse<List<ProfileResponse.GetFollowersResponse>> {
        return executeApiCall { profileApi.getFollowers(token, memberId, page) }
    }

    suspend fun getFollowings(
        token: String,
        memberId: Int? = null,
        page: Int
    ): ApiResponse<List<ProfileResponse.GetFollowingsResponse>> {
        return executeApiCall { profileApi.getFollowings(token, memberId, page) }
    }
}
