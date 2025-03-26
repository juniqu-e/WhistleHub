package com.whistlehub.common.data.remote.api

import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.RankingResponse
import retrofit2.Response
import retrofit2.http.*

/**
-----------------------
랭킹 관련 API 인터페이스
-----------------------
 **/

interface RankingApi {
    // 랭킹 조회
    @GET("ranking")
    suspend fun getRanking(
        @Query("rankingType") rankingType: String,
        @Query("period") period: String,
        @Query("tag") tag: String? = null
    ): Response<ApiResponse<List<RankingResponse.GetRankingResponse>>>

    @GET("recommend")
    suspend fun getRecommendTrack(
        @Query("trackId") trackId: Int?
    ) :Response<ApiResponse<List<RankingResponse.RecommendTrackResponse>>>
}