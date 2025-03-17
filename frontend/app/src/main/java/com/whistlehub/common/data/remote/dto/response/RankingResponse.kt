package com.whistlehub.common.data.remote.dto.response

sealed class RankingResponse {
    data class RankingList(
        val tracks: List<RankingTrack>,
        val category: String,
        val timeRange: String,
        val updateDate: String
    )

    data class RankingTrack(
        val trackId: String,
        val rank: Int,
        val previousRank: Int?, // null if new entry
        val title: String,
        val artistName: String,
        val playCount: Int,
        val likesCount: Int,
        val duration: Int,
        val imageUrl: String?
    )
}