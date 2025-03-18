package com.whistlehub.common.data.remote.dto.request

import okhttp3.MultipartBody

/**
-----------------------
트랙 관련 API 요청 DTO
-----------------------
 **/

sealed class TrackRequest {
    // 트랙 정보 수정
    data class UpdateTrackRequest(
        val trackId: String,
        val title: String,
        val description: String,
        val imageUrl: String,
        val visibility: Boolean
    )

    data class UploadTrackImageRequest(
        val image: MultipartBody.Part
    )

    data class TrackPlayCountRequest(
        val trackId: Int
    )

    data class AddTrackToPlaylistRequest(
        val playlistId: Int,
        val trackId: Int
    )

    data class LikeTrackRequest(
        val trackId: Int,
        val like: Boolean
    )

    data class CreateCommentRequest(
        val trackId: Int,
        val comment: String
    )

    data class UpdateCommentRequest(
        val commentId: Int,
        val comment: String
    )

    data class SearchTrackRequest(
        val keyword: String?,
        val tags: List<String>?
    )

    data class ReportTrackRequest(
        val trackId: Int,
        val reason: String,
        val detail: String?,
        val layerId: List<Int>?
    )
}