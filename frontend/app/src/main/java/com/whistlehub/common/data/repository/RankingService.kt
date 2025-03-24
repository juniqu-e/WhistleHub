package com.whistlehub.common.data.repository

import com.whistlehub.common.data.remote.api.RankingApi
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.RankingResponse
import com.whistlehub.common.util.TokenRefresh
import javax.inject.Inject
import javax.inject.Singleton

/**
--------------------------------------
랭킹 관련 API 호출을 담당하는 서비스 클래스
--------------------------------------
 **/

@Singleton
class RankingService @Inject constructor(
    private val rankingApi: RankingApi,
    private val tokenRefresh: TokenRefresh? = null
) : ApiRepository() {
    // 랭킹 조회
    suspend fun getRanking(
        token: String,
        rankingType: String,
        period: String,
        tag: String? = null
    ): ApiResponse<List<RankingResponse.GetRankingResponse>> {
        return executeApiCall { rankingApi.getRanking(token, rankingType, period, tag) }
    }
    // 추천 트랙 조회
    suspend fun getRecommendTrack(
        token: String,
        trackId: Int?
    ): ApiResponse<List<RankingResponse.RecommendTrackResponse>> {
        return executeApiCall { rankingApi.getRecommendTrack(token, trackId) }
    }
}