package com.whistlehub.common.data.remote.dto.request

import okhttp3.MultipartBody

sealed class PlaylistRequest {
    data class CreatePlaylistRequset(
        val name: String,
        val description: String?,
//        val image: MultipartBody.Part?,
        val trackIds: List<Int>?
    )

    data class UpdatePlaylistRequest(
        val playlistId: Int,
        val name: String,
        val description: String,
//        val imageUrl: String 여기에 Url 들어가는지? 아니면 file 들어가는지?
    )

    data class UpdatePlaylistTrackRequest(
        val playlistId: Int,
        val tracks: List<Int>?
    )

    data class UploadPlaylistImageRequest(
        val image: MultipartBody.Part
    )
}