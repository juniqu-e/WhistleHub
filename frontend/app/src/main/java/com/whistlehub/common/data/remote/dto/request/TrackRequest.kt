package com.whistlehub.common.data.remote.dto.request

sealed class TrackRequest {
    data class UpdateTrack(
        val trackId: String,
        val title: String,
        val description: String,
        val isPublic: Boolean,
        val genre: String,
        val tags: List<String>
    )

    data class SearchTrack(
        val keyword: String,
        val page: Int,
        val size: Int,
        val orderBy: String // "RECENT", "POPULAR", etc.
    )

    data class LikeTrack(
        val trackId: String
    )

    data class ReportTrack(
        val trackId: String,
        val reason: String
    )

    data class AddToPlaylist(
        val trackId: String,
        val playlistId: String
    )

    data class CreateComment(
        val trackId: String,
        val content: String,
        val parentCommentId: String?
    )

    data class UpdateComment(
        val commentId: String,
        val content: String
    )

    data class IncreasePlayCount(
        val trackId: String
    )
}