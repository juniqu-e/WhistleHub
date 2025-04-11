package com.whistlehub.common.data.remote.dto.response

import com.google.gson.JsonObject

sealed class TrackResponse {
    // 트랙 상세 조회
    data class GetTrackDetailResponse(
        val trackId: Int,
        val title: String,
        val description: String?,
        val duration: Int,
        val imageUrl: String?,
        val artist: ArtistInfo,
        val isLiked: Boolean,
        val importCount: Int,
        val likeCount: Int,
        val viewCount: Int,
        val createdAt: String,
        val sourceTrack: List<TrackSummary>,
        val importTrack: List<TrackSummary>,
        val tags: List<TagInfo>?
    )

    // 작곡가 정보
    data class ArtistInfo(
        val memberId: Int,
        val nickname: String,
        val profileImage: String?
    )

    // 태그 정보
    data class TagInfo(
        val tagId: Int,
        val name: String
    )

    // 소스 트랙 요약 정보
    data class TrackSummary(
        val trackId: Int,
        val title: String,
        val duration: Int,
        val imageUrl: String?
    )

    // 트랙 레이어 조회
    data class GetTrackLayer(
        val layerId: Int,
        val name: String,
        val instrumentType: String
    )

    // 트랙 레이어 재생
    data class TrackLayerPlay(
        val soundUrl: String,
        val modification: JsonObject
    )

    // 트랙 댓글 조회
    data class GetTrackComment(
        val commentId: Int,
        val memberInfo: MemberInfo,
        val comment: String
    )

    // 트랙에 댓글 단 멤버
    data class MemberInfo(
        val memberId: Int,
        val nickname: String,
        val profileImage: String
    )

    // 트랙 검색
    data class SearchTrack(
        val trackId: Int,
        val nickname: String,
        val title: String,
        val duration: Int,
        val imageUrl: String?
    )
}