package com.whistlehub.common.data.repository

import com.whistlehub.common.data.remote.api.ProfileApi
import com.whistlehub.common.data.remote.dto.request.ProfileRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.ProfileResponse
import com.whistlehub.common.util.TokenRefresh
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
---------------------------------------
프로필 관련 API 호출을 담당하는 서비스 클래스
---------------------------------------
 **/

@Singleton
class ProfileService @Inject constructor(
    private val profileApi: ProfileApi,
) : ApiRepository() {
    // 프로필 조회 (memberId가 생략 된 경우 AccessToken의 memberId 조회)
    suspend fun getProfile(
        memberId: Int?
    ): ApiResponse<ProfileResponse.GetProfileResponse> {
        return executeApiCall { profileApi.getProfile(memberId) }
    }
    // 프로필 정보 수정
    suspend fun updateProfile(
        request: ProfileRequest.UpdateProfileRequest
    ): ApiResponse<Unit> {
        return executeApiCall { profileApi.updateProfile(request) }
    }
    // 회원 탈퇴
    suspend fun deleteProfile(
    ): ApiResponse<Unit> {
        return executeApiCall { profileApi.deleteProfile() }
    }
    // 프로필 사진 업로드 (response : 업로드 된 이미지 링크)
    suspend fun uploadProfileImage(
        memberId: Int,
        image: MultipartBody.Part
    ): ApiResponse<String> {
        val memberIdBody: RequestBody = memberId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        return executeApiCall { profileApi.uploadProfileImage(memberIdBody, image) }
    }
    // 비밀번호 변경
    suspend fun changePassword(
        request: ProfileRequest.ChangePasswordRequest
    ): ApiResponse<Unit> {
        return executeApiCall { profileApi.changePassword(request) }
    }
    // 멤버 검색
    suspend fun searchProfile(
        query: String,
        page: Int,
        size: Int,
        orderby: String
    ): ApiResponse<List<ProfileResponse.SearchProfileResponse>> {
        return executeApiCall { profileApi.searchProfile(query, page, size, orderby) }
    }
    // 멤버의 플레이리스트 조회 (memberId가 생략 된 경우 AccessToken의 memberId 조회)
    suspend fun getMemberPlaylists(
        memberId: Int? = null
    ): ApiResponse<List<ProfileResponse.GetMemberPlaylistsResponse>> {
        return executeApiCall { profileApi.getMemberPlaylists(memberId) }
    }
    // 멤버의 트랙 조회 (memberId가 생략 된 경우 AccessToken의 memberId 조회)
    suspend fun getMemberTracks(
        memberId: Int? = null,
        page: Int,
        orderby: String
    ): ApiResponse<List<ProfileResponse.GetMemberTracksResponse>> {
        return executeApiCall { profileApi.getMemberTracks(memberId, page, orderby) }
    }
    // 팔로우
    suspend fun follow(
        request: ProfileRequest.FollowRequest
    ): ApiResponse<Unit> {
        return executeApiCall { profileApi.follow(request) }
    }
    // 멤버의 팔로워 목록 조회 (memberId가 생략 된 경우 AccessToken의 memberId 조회)
    suspend fun getFollowers(
        memberId: Int? = null,
        page: Int
    ): ApiResponse<List<ProfileResponse.GetFollowersResponse>> {
        return executeApiCall { profileApi.getFollowers(memberId, page) }
    }
    // 멤버의 팔로잉 목록 조회 (memberId가 생략 된 경우 AccessToken의 memberId 조회)
    suspend fun getFollowings(
        token: String,
        memberId: Int? = null,
        page: Int
    ): ApiResponse<List<ProfileResponse.GetFollowingsResponse>> {
        return executeApiCall { profileApi.getFollowings( memberId, page) }
    }
}
