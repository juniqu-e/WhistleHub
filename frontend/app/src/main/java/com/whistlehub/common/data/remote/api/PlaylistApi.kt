package com.whistlehub.common.data.remote.api

import com.whistlehub.common.data.remote.dto.request.PlaylistRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.PlaylistResponse
import retrofit2.Response
import retrofit2.http.*

/**
-----------------------
플레이리스트 관련 API 인터페이스
-----------------------
 **/

interface PlaylistApi {
    @GET("playlist")
    suspend fun getPlaylists(
        @Header("Authorization") token: String,
        @Query("playlistId") playlistId: Int
    ): Response<ApiResponse<List<PlaylistResponse.GetPlaylistResponse>>>

    @POST("playlist")
    suspend fun createPlaylist(
        @Header("Authorization") token: String,
        @Body request: PlaylistRequest.CreatePlaylistRequset
    ): Response<ApiResponse<Int>>

    @PUT("playlist")
    suspend fun updatePlaylist(
        @Header("Authorization") token: String,
        @Body request: PlaylistRequest.UpdatePlaylistRequest
    ): Response<ApiResponse<Unit>>

    @DELETE("playlist")
    suspend fun deletePlaylist(
        @Header("Authorization") token: String,
        @Query("playlistId") playlistId: Int
    ): Response<ApiResponse<Unit>>

    @GET("playlist/track")
    suspend fun getPlaylistTracks(
        @Header("Authorization") token: String,
        @Query("playlistId") playlistId: Int
    ): Response<ApiResponse<List<PlaylistResponse.PlaylistTrackResponse>>>

    @PUT("playlist/track")
    suspend fun updatePlaylistTracks(
        @Header("Authorization") token: String,
        @Body request: PlaylistRequest.UpdatePlaylistTrackRequest
    ): Response<ApiResponse<Unit>>

    @Multipart
    @POST("playlist/image")
    suspend fun uploadPlaylistImage(
        @Header("Authorization") token: String,
        @Body request: PlaylistRequest.UploadPlaylistImageRequest
    ): Response<ApiResponse<String>>
}
