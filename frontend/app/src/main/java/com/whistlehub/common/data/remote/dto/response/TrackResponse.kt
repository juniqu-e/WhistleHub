package com.whistlehub.common.data.remote.dto.response

sealed class TrackResponse {
    data class TrackDetail(
        val trackId: String,
        val title: String,
        val description: String,
        val creatorId: String,
        val creatorNickname: String,
        val creatorProfileImage: String?,
        val duration: Int,
        val playCount: Int,
        val likesCount: Int,
        val isLiked: Boolean,
        val isPublic: Boolean,
        val genre: String,
        val tags: List<String>,
        val imageUrl: String?,
        val createdAt: String,
        val updatedAt: String
    )

    data class Comment(
        val commentId: String,
        val memberId: String,
        val nickname: String,
        val profileImage: String?,
        val content: String,
        val createdAt: String,
        val updatedAt: String,
        val isMyComment: Boolean,
        val replies: List<Comment>?
    )

    data class Layer(
        val layerId: String,
        val trackId: String,
        val name: String,
        val type: String, // "VOCAL", "DRUM", "BASS", etc.
        val duration: Int,
        val isBase: Boolean
    )

    data class SearchResult(
        val tracks: List<TrackSummary>,
        val totalCount: Int,
        val hasNext: Boolean
    )

    data class TrackSummary(
        val trackId: String,
        val title: String,
        val artistName: String,
        val playCount: Int,
        val likesCount: Int,
        val duration: Int,
        val imageUrl: String?
    )
}