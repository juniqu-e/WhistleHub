package com.whistlehub.common.data.repository

import com.whistlehub.common.data.remote.api.PlaylistApi
import com.whistlehub.common.data.remote.dto.request.PlaylistRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.PlaylistResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistService @Inject constructor(
    private val playlistApi: PlaylistApi
) : ApiRepository() {

    suspend fun getPlaylists(
        token: String,
        playlistId: Int
    ): ApiResponse<List<PlaylistResponse.GetPlaylistResponse>> {
        return executeApiCall { playlistApi.getPlaylists(token, playlistId) }
    }

    suspend fun createPlaylist(
        token: String,
        request: PlaylistRequest.CreatePlaylistRequset
    ): ApiResponse<Int> {
        return executeApiCall { playlistApi.createPlaylist(token, request) }
    }

    suspend fun updatePlaylist(
        token: String,
        request: PlaylistRequest.UpdatePlaylistRequest
    ): ApiResponse<Unit> {
        return executeApiCall { playlistApi.updatePlaylist(token, request) }
    }

    suspend fun deletePlaylist(
        token: String,
        playlistId: Int
    ): ApiResponse<Unit> {
        return executeApiCall { playlistApi.deletePlaylist(token, playlistId) }
    }

    suspend fun getPlaylistTracks(
        token: String,
        playlistId: Int
    ): ApiResponse<List<PlaylistResponse.PlaylistTrackResponse>> {
        return executeApiCall { playlistApi.getPlaylistTracks(token, playlistId) }
    }

    suspend fun updatePlaylistTracks(
        token: String,
        request: PlaylistRequest.UpdatePlaylistTrackRequest
    ): ApiResponse<Unit> {
        return executeApiCall { playlistApi.updatePlaylistTracks(token, request) }
    }

    suspend fun uploadPlaylistImage(
        token: String,
        request: PlaylistRequest.UploadPlaylistImageRequest
    ): ApiResponse<String> {
        return executeApiCall { playlistApi.uploadPlaylistImage(token, request) }
    }
}
