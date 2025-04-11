package com.whistlehub.common.data.remote.dto.request

/**
---------------------
트랙 관련 API 요청 DTO
---------------------
 **/
sealed class TrackRequest {
    // 트랙 정보 수정
    data class UpdateTrackRequest(
        val trackId: String,
        val title: String,
        val description: String,
        val visibility: Boolean
    )

    // 트랙 재생 카운트
    data class TrackPlayCountRequest(
        val trackId: Int
    )

    // 플레이리스트 트랙 추가
    data class AddTrackToPlaylistRequest(
        val playlistId: Int,
        val trackId: Int
    )

    // 트랙 좋아요 / 좋아요 취소
    data class LikeTrackRequest(
        val trackId: Int
    )

    // 트랙 댓글 작성
    data class CreateCommentRequest(
        val trackId: Int,
        val context: String
    )

    // 트랙 댓글 수정
    data class UpdateCommentRequest(
        val commentId: Int,
        val context: String
    )

    // 트랙 검색
    data class SearchTrackRequest(
        val keyword: String,
        val page: Int,
        val size: Int,
        val orderBy: String,
    )

//    data class SearchTrackRequest(
//        val keyword: String?,
//        val tags: List<String>?
//    )

    // 트랙 신고
    data class ReportTrackRequest(
        val trackId: Int,
        val type: Int,
        val detail: String?,
    )
}