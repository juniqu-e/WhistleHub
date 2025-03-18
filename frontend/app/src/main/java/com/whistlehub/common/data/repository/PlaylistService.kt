package com.whistlehub.common.data.repository

import com.whistlehub.common.data.remote.api.PlaylistApi
import com.whistlehub.common.data.remote.dto.request.PlaylistRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.PlaylistResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
---------------------------------------------
플레이리스트 관련 API 호출을 담당하는 서비스 클래스
---------------------------------------------
 **/

@Singleton
class PlaylistService @Inject constructor(
    private val playlistApi: PlaylistApi
) : ApiRepository() {
    // 플레이리스트 조회
    suspend fun getPlaylists(
        token: String,
        playlistId: Int
    ): ApiResponse<List<PlaylistResponse.GetPlaylistResponse>> {
        return executeApiCall { playlistApi.getPlaylists(token, playlistId) }
    }
    // 플레이리스트 생성
    suspend fun createPlaylist(
        token: String,
        request: PlaylistRequest.CreatePlaylistRequest
    ): ApiResponse<Int> {
        return executeApiCall { playlistApi.createPlaylist(token, request) }
    }
    // 플레이리스트 수정
    suspend fun updatePlaylist(
        token: String,
        request: PlaylistRequest.UpdatePlaylistRequest
    ): ApiResponse<Unit> {
        return executeApiCall { playlistApi.updatePlaylist(token, request) }
    }
    // 플레이리스트 삭제
    suspend fun deletePlaylist(
        token: String,
        playlistId: Int
    ): ApiResponse<Unit> {
        return executeApiCall { playlistApi.deletePlaylist(token, playlistId) }
    }
    // 플레이리스트 내부 조회
    suspend fun getPlaylistTracks(
        token: String,
        playlistId: Int
    ): ApiResponse<List<PlaylistResponse.PlaylistTrackResponse>> {
        return executeApiCall { playlistApi.getPlaylistTracks(token, playlistId) }
    }
    // 플레이리스트 내부 수정 (위치 이동, 삭제)
    suspend fun updatePlaylistTracks(
        token: String,
        request: PlaylistRequest.UpdatePlaylistTrackRequest
    ): ApiResponse<Unit> {
        return executeApiCall { playlistApi.updatePlaylistTracks(token, request) }
    }
    // 플레이리스트 이미지 업로드
    suspend fun uploadPlaylistImage(
        token: String,
        playlistId: Int,
        image: MultipartBody.Part
    ): ApiResponse<Unit> {
        val playlistIdBody: RequestBody = playlistId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        return executeApiCall { playlistApi.uploadPlaylistImage(token, playlistIdBody, image) }
    }
}
