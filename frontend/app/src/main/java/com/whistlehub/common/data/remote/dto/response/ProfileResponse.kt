package com.whistlehub.common.data.remote.dto.response

sealed class ProfileResponse {
    data class GetProfileResponse(
        val nickname: String,
        val profileImage: String,
        val profileText: String
    )

    data class SearchProfileResponse(
        val memberId: Int,
        val nickname: String,
        val profileImage: String
    )

    data class GetMemberPlaylistsResponse(
        val playlistId: Int,
        val imageUrl: String
    )

    data class GetMemberTracksResponse(
        val trackId: Int,
        val nickname: String,
        val title: String,
        val duration: Int,
        val imageUrl: String?
    )

    data class GetFollowersResponse(
        val memberId: Int,
        val profileImage: String?,
        val nickname: String
    )

    data class GetFollowingsResponse(
        val memberId: Int,
        val profileImage: String?,
        val nickname: String
    )
}