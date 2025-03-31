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
    private val tokenRefresh: TokenRefresh
) : ApiRepository() {
    // 랭킹 조회
    suspend fun getRanking(
        rankingType: String,
        period: String,
        tag: String? = null
    ): ApiResponse<List<RankingResponse.GetRankingResponse>> {
        return tokenRefresh.execute { rankingApi.getRanking(rankingType, period, tag) }
    }
    // 추천 트랙 조회
    suspend fun getRecommendTrack(
        trackId: Int?
    ): ApiResponse<List<RankingResponse.RecommendTrackResponse>> {
        return tokenRefresh.execute { rankingApi.getRecommendTrack(trackId) }
    }
}