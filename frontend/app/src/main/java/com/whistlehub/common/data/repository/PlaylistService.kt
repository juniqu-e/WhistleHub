package com.whistlehub.common.data.repository

import com.whistlehub.common.data.remote.api.PlaylistApi
import com.whistlehub.common.data.remote.dto.request.PlaylistRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.PlaylistResponse
import com.whistlehub.common.util.TokenRefresh
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
    private val playlistApi: PlaylistApi,
    private val tokenRefresh: TokenRefresh
) : ApiRepository() {
    // 특정 멤버의 플레이리스트 목록 조회
    suspend fun getMemberPlaylists(
        memberId: Int,
        page: Int,
        size: Int,
        orderby: String = "ASC"
    ): ApiResponse<List<PlaylistResponse.GetMemberPlaylistsResponse>> {
        return tokenRefresh.execute { playlistApi.getMemberPlaylists(memberId, page, size, orderby) }
    }
    // 플레이리스트 조회
    suspend fun getPlaylists(
        playlistId: Int
    ): ApiResponse<PlaylistResponse.GetPlaylistResponse> {
        return tokenRefresh.execute { playlistApi.getPlaylists(playlistId) }
    }
    // 플레이리스트 생성
    suspend fun createPlaylist(
        name: String,
        description: String? = null,
        trackIds: List<Int>? = null,
        image: MultipartBody.Part? = null
    ): ApiResponse<Int> {
        val nameBody: RequestBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val descriptionBody: RequestBody = description?.toRequestBody("text/plain".toMediaTypeOrNull()) ?: "".toRequestBody("text/plain".toMediaTypeOrNull())
        val trackIdsBody: RequestBody = trackIds?.joinToString(",")?.toRequestBody("text/plain".toMediaTypeOrNull()) ?: "".toRequestBody("text/plain".toMediaTypeOrNull())
        return tokenRefresh.execute { playlistApi.createPlaylist(nameBody, descriptionBody, trackIdsBody, image) }
    }
    // 플레이리스트 수정
    suspend fun updatePlaylist(
        request: PlaylistRequest.UpdatePlaylistRequest
    ): ApiResponse<Unit> {
        return tokenRefresh.execute { playlistApi.updatePlaylist(request) }
    }
    // 플레이리스트 삭제
    suspend fun deletePlaylist(
        playlistId: Int
    ): ApiResponse<Unit> {
        return tokenRefresh.execute { playlistApi.deletePlaylist(playlistId) }
    }
    // 플레이리스트 내부 조회
    suspend fun getPlaylistTracks(
        playlistId: Int
    ): ApiResponse<List<PlaylistResponse.PlaylistTrackResponse>> {
        return tokenRefresh.execute { playlistApi.getPlaylistTracks(playlistId) }
    }
    // 플레이리스트에 트랙 추가
    suspend fun addTrackToPlaylist(
        request: PlaylistRequest.AddTrackToPlaylistRequest
    ): ApiResponse<Unit> {
        return tokenRefresh.execute { playlistApi.addTrackToPlaylist(request) }
    }
    // 플레이리스트 내부 수정 (위치 이동, 삭제)
    suspend fun updatePlaylistTracks(
        request: PlaylistRequest.UpdatePlaylistTrackRequest
    ): ApiResponse<Unit> {
        return tokenRefresh.execute { playlistApi.updatePlaylistTracks(request) }
    }
    // 플레이리스트 이미지 업로드
    suspend fun uploadPlaylistImage(
        playlistId: Int,
        image: MultipartBody.Part
    ): ApiResponse<Unit> {
        val playlistIdBody: RequestBody = playlistId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        return tokenRefresh.execute { playlistApi.uploadPlaylistImage(playlistIdBody, image) }
    }
}
