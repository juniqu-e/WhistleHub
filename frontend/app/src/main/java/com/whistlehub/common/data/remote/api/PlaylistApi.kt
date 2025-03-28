package com.whistlehub.common.data.remote.api

import com.whistlehub.common.data.remote.dto.request.PlaylistRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.PlaylistResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

/**
----------------------------
플레이리스트 관련 API 인터페이스
----------------------------
 **/

interface PlaylistApi {
    // 특정 멤버의 플레이리스트 목록 조회
    @GET("playlist/member")
    suspend fun getMemberPlaylists(
        @Query("memberId") memberId: Int,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("orderby") orderby: String
    ): Response<ApiResponse<List<PlaylistResponse.GetMemberPlaylistsResponse>>>
    // 플레이리스트 조회
    @GET("playlist")
    suspend fun getPlaylists(
        @Query("playlistId") playlistId: Int
    ): Response<ApiResponse<PlaylistResponse.GetPlaylistResponse>>
    // 플레이리스트 생성
    @Multipart
    @POST("playlist")
    suspend fun createPlaylist(
        @Part("name") name : RequestBody,
        @Part("description") description : RequestBody,
        @Part("trackIds") trackIds : RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<ApiResponse<Int>>
    // 플레이리스트 수정
    @PUT("playlist")
    suspend fun updatePlaylist(
        @Body request: PlaylistRequest.UpdatePlaylistRequest
    ): Response<ApiResponse<Unit>>
    // 플레이리스트 삭제
    @DELETE("playlist")
    suspend fun deletePlaylist(
        @Query("playlistId") playlistId: Int
    ): Response<ApiResponse<Unit>>
    // 플레이리스트 내부 조회
    @GET("playlist/track")
    suspend fun getPlaylistTracks(
        @Query("playlistId") playlistId: Int
    ): Response<ApiResponse<List<PlaylistResponse.PlaylistTrackResponse>>>
    // 플레이리스트에 트랙 추가
    @POST("playlist/track")
    suspend fun addTrackToPlaylist(
        @Body request: PlaylistRequest.AddTrackToPlaylistRequest
    ): Response<ApiResponse<Unit>>
    // 플레이리스트 내부 수정 (위치 이동, 삭제)
    @PUT("playlist/track")
    suspend fun updatePlaylistTracks(
        @Body request: PlaylistRequest.UpdatePlaylistTrackRequest
    ): Response<ApiResponse<Unit>>
    // 플레이리스트 이미지 업로드
    @Multipart
    @POST("playlist/image")
    suspend fun uploadPlaylistImage(
        @Part("playlistId") playlistId: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<ApiResponse<Unit>>
}
