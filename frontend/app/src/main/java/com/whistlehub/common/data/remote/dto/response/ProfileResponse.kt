package com.whistlehub.common.data.remote.dto.response

sealed class ProfileResponse {
    data class Profile(
        val memberId: String,
        val nickname: String,
        val profileImage: String?,
        val profileText: String?,
        val followingCount: Int,
        val followerCount: Int,
        val isFollowing: Boolean,
        val isMe: Boolean
    )

    data class Follow(
        val memberId: String,
        val nickname: String,
        val profileImage: String?,
        val isFollowing: Boolean
    )

    data class MemberTrack(
        val trackId: Int,
        val nickname: String,
        val title: String,
        val duration: Int,
        val imageUrl: String?
    )
}