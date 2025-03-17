package com.whistlehub.common.data.remote.dto.request

import okhttp3.MultipartBody

sealed class PlaylistRequest {
    data class Create(
        val name: String,
        val description: String,
        val isPublic: Boolean
    )

    data class Update(
        val playlistId: String,
        val name: String,
        val description: String,
        val isPublic: Boolean
    )

    data class UpdateTracks(
        val playlistId: String,
        val trackIds: List<String>
    )
}