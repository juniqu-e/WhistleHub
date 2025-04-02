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
    private val tokenRefresh: TokenRefresh
) : ApiRepository() {
    // 프로필 조회
    suspend fun getProfile(
        memberId: Int
    ): ApiResponse<ProfileResponse.GetProfileResponse> {
        return tokenRefresh.execute { profileApi.getProfile(memberId) }
    }
    // 프로필 정보 수정
    suspend fun updateProfile(
        request: ProfileRequest.UpdateProfileRequest
    ): ApiResponse<Unit> {
        return tokenRefresh.execute { profileApi.updateProfile(request) }
    }
    // 회원 탈퇴
    suspend fun deleteProfile(
    ): ApiResponse<Unit> {
        return tokenRefresh.execute { profileApi.deleteProfile() }
    }
    // 프로필 사진 업로드 (response : 업로드 된 이미지 링크)
    suspend fun uploadProfileImage(
        memberId: Int,
        image: MultipartBody.Part
    ): ApiResponse<String> {
        val memberIdBody: RequestBody = memberId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        return tokenRefresh.execute { profileApi.uploadProfileImage(memberIdBody, image) }
    }
    // 비밀번호 변경
    suspend fun changePassword(
        request: ProfileRequest.ChangePasswordRequest
    ): ApiResponse<Unit> {
        return tokenRefresh.execute { profileApi.changePassword(request) }
    }
    // 멤버 검색
    suspend fun searchProfile(
        query: String,
        page: Int,
        size: Int,
    ): ApiResponse<List<ProfileResponse.SearchProfileResponse>> {
        return tokenRefresh.execute { profileApi.searchProfile(query, page, size) }
    }
    // 멤버의 플레이리스트 조회
    suspend fun getMemberPlaylists(
        memberId: Int
    ): ApiResponse<List<ProfileResponse.GetMemberPlaylistsResponse>> {
        return tokenRefresh.execute { profileApi.getMemberPlaylists(memberId) }
    }
    // 멤버의 트랙 조회
    suspend fun getMemberTracks(
        memberId: Int,
        page: Int,
        size: Int
    ): ApiResponse<List<ProfileResponse.GetMemberTracksResponse>> {
        return tokenRefresh.execute { profileApi.getMemberTracks(memberId, page, size) }
    }
    // 팔로우
    suspend fun follow(
        request: ProfileRequest.FollowRequest
    ): ApiResponse<Unit> {
        return tokenRefresh.execute { profileApi.follow(request) }
    }
    // 멤버의 팔로워 목록 조회
    suspend fun getFollowers(
        memberId: Int,
        page: Int,
        size: Int
    ): ApiResponse<List<ProfileResponse.GetFollowersResponse>> {
        return tokenRefresh.execute { profileApi.getFollowers(memberId, page, size) }
    }
    // 멤버의 팔로잉 목록 조회
    suspend fun getFollowings(
        memberId: Int,
        page: Int,
        size: Int
    ): ApiResponse<List<ProfileResponse.GetFollowingsResponse>> {
        return tokenRefresh.execute { profileApi.getFollowings( memberId, page, size) }
    }
}
