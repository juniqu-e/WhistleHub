package com.whistlehub.common.data.remote.dto.response

/**
----------------------
프로필 관련 API 응답 DTO
----------------------
 **/

sealed class ProfileResponse {
    // 프로필 조회
    data class GetProfileResponse(
        val nickname: String,
        val profileImage: String,
        val profileText: String
    )
    // 멤버 검색
    data class SearchProfileResponse(
        val memberId: Int,
        val nickname: String,
        val profileImage: String
    )
    // 멤버의 플레이리스트 조회
    data class GetMemberPlaylistsResponse(
        val playlistId: Int,
        val imageUrl: String
    )
    // 멤버의 트랙 조회
    data class GetMemberTracksResponse(
        val trackId: Int,
        val nickname: String,
        val title: String,
        val duration: Int,
        val imageUrl: String?
    )
    // 멤버의 팔로워 목록 조회
    data class GetFollowersResponse(
        val memberId: Int,
        val profileImage: String?,
        val nickname: String
    )
    // 멤버의 팔로잉 목록 조회
    data class GetFollowingsResponse(
        val memberId: Int,
        val profileImage: String?,
        val nickname: String
    )
}