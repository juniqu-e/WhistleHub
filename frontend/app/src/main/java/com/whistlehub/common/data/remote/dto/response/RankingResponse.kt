package com.whistlehub.common.data.remote.dto.response

/**
---------------------
랭킹 관련 API 응답 DTO
---------------------
 **/

sealed class RankingResponse {
    // 랭킹 조회
    data class GetRankingResponse(
        val trackId: Int,
        val nickname: String,
        val title: String,
        val duration: Int,
        val imageUrl: String?
    )
    // 추천 목록 조회
    data class RecommendTrackResponse(
        val trackId: Int,
        val nickname: String,
        val title: String,
        val duration: Int,
        val imageUrl: String?
    )
}