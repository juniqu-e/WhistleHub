package com.whistlehub.common.data.remote.dto.response

sealed class PlaylistResponse {

    data class GetPlaylistResponse(
        val memberId: Int,
        val name: String,
        val description: String?,
        val imageUrl: String?
    )

    data class Track(
        val trackId: Int,
        val nickname: String,
        val title: String,
        val duration: Int,
        val imageUrl: String?
    )

    data class PlaylistTrackResponse(
        val playlistTrackId: Int,
        val playOrder: Double?,
        val trackInfo: Track
    )
}