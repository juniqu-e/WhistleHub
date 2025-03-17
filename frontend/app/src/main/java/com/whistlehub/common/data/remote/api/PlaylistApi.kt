package com.whistlehub.common.data.remote.api

import com.whistlehub.common.data.remote.dto.request.PlaylistRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.PlaylistResponse
import retrofit2.http.*

interface PlaylistApi {
    @POST("playlist")
    suspend fun createPlaylist(@Body request: PlaylistRequest.Create): ApiResponse<PlaylistResponse.Playlist>

    @GET("playlist")
    suspend fun getPlaylists(@Query("page") page: Int, @Query("size") size: Int): ApiResponse<List<PlaylistResponse.Playlist>>

    @GET("playlist/track")
    suspend fun getPlaylistTracks(@Query("playlistId") playlistId: String): ApiResponse<List<PlaylistResponse.Track>>

    @DELETE("playlist")
    suspend fun deletePlaylist(@Query("playlistId") playlistId: String): ApiResponse<Unit>

    @PUT("playlist")
    suspend fun updatePlaylist(@Body request: PlaylistRequest.Update): ApiResponse<Unit>

    @Multipart
    @POST("playlist/image")
    suspend fun uploadPlaylistImage(
        @Query("playlistId") playlistId: String,
        @Part image: MultipartBody.Part
    ): ApiResponse<String>

    @PUT("playlist/track")
    suspend fun updatePlaylistTracks(@Body request: PlaylistRequest.UpdateTracks): ApiResponse<Unit>
}