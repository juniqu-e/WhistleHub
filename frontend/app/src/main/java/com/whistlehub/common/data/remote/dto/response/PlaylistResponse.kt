package com.whistlehub.common.data.remote.dto.response

sealed class PlaylistResponse {
    data class Playlist(
        val playlistId: String,
        val name: String,
        val description: String,
        val isPublic: Boolean,
        val imageUrl: String?,
        val trackCount: Int,
        val createdAt: String,
        val updatedAt: String
    )

    data class Track(
        val trackId: String,
        val title: String,
        val artistName: String,
        val duration: Int,
        val imageUrl: String?
    )

    data class MemberPlaylist(
        val playlistId: Int,
        val imageUrl: String
    )
}