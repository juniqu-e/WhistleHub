package com.whistlehub.common.data.remote.dto.response

/**
---------------------------
플레이리스트 관련 API 응답 DTO
---------------------------
 **/

sealed class PlaylistResponse {
    // 플레이리스트 조회
    data class GetPlaylistResponse(
        val memberId: Int,
        val name: String,
        val description: String?,
        val imageUrl: String?
    )
    // 플레이리스트의 개별 트랙
    data class Track(
        val trackId: Int,
        val nickname: String,
        val title: String,
        val duration: Int,
        val imageUrl: String?
    )
    // 플레이리스트 내부 조회
    data class PlaylistTrackResponse(
        val playlistTrackId: Int,
        val playOrder: Double?,
        val trackInfo: Track
    )
}