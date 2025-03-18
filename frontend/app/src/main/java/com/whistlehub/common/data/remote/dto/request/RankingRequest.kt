package com.whistlehub.common.data.remote.dto.request

sealed class RankingRequest {
    data class GetRanking(
        val category: String, // "POPULAR", "TRENDING", "NEW", etc.
        val timeRange: String, // "DAILY", "WEEKLY", "MONTHLY", etc.
        val page: Int,
        val size: Int
    )
}