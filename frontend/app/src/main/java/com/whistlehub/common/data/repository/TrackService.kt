package com.whistlehub.common.data.repository

import com.whistlehub.common.data.remote.api.TrackApi
import com.whistlehub.common.data.remote.dto.request.TrackRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.TrackResponse
import com.whistlehub.common.util.TokenRefresh
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
---------------------------------------
트랙 관련 API 호출을 담당하는 서비스 클래스
---------------------------------------
 **/
@Singleton
class TrackService @Inject constructor(
    private val trackApi: TrackApi
) : ApiRepository() {
    // 트랙 상세 조회
    suspend fun getTrackDetail(
        trackId: String
    ): ApiResponse<TrackResponse.GetTrackDetailResponse> {
        return executeApiCall { trackApi.getTrackDetail(trackId) }
    }

    // 트랙 정보 수정
    suspend fun updateTrack(
        request: TrackRequest.UpdateTrackRequest
    ): ApiResponse<Unit> {
        return executeApiCall { trackApi.updateTrack(request) }
    }

    // 트랙 삭제
    suspend fun deleteTrack(
        trackId: String
    ): ApiResponse<Unit> {
        return executeApiCall { trackApi.deleteTrack(trackId) }
    }

    // 트랙 재생 요청
    suspend fun playTrack(
        trackId: String,
    ): ByteArray? {
        val response = trackApi.playTrack(trackId) // API 호출
        return if (response.isSuccessful) {
            response.body()?.bytes() // 성공하면 ByteArray 반환
        } else {
            null // 실패 시 null 반환
        }
    }

    // 트랙 재생 카운트 증가
    suspend fun increasePlayCount(
        request: TrackRequest.TrackPlayCountRequest
    ): ApiResponse<Unit> {
        return executeApiCall { trackApi.increasePlayCount(request) }
    }

    // 플레이리스트에 트랙 추가
    suspend fun addTrackToPlaylist(
        request: TrackRequest.AddTrackToPlaylistRequest
    ): ApiResponse<Unit> {
        return executeApiCall { trackApi.addTrackToPlaylist(request) }
    }

    // 트랙 레이어 조회
    suspend fun getTrackLayers(
        trackId: String
    ): ApiResponse<List<TrackResponse.GetTrackLayer>> {
        return executeApiCall { trackApi.getTrackLayers(trackId) }
    }

    // 트랙 레이어 재생
    suspend fun playLayer(
        layerId: String
    ): ApiResponse<TrackResponse.TrackLayerPlay> {
        return executeApiCall { trackApi.playLayer(layerId) }
    }

    // 트랙 좋아요 상태 조회
    suspend fun getTrackLikeStatus(
        trackId: String
    ): ApiResponse<Boolean> {
        return executeApiCall { trackApi.getTrackLikeStatus(trackId) }
    }

    // 트랙 좋아요
    suspend fun likeTrack(
        request: TrackRequest.LikeTrackRequest
    ): ApiResponse<Unit> {
        return executeApiCall { trackApi.likeTrack(request) }
    }

    // 트랙 댓글 조회
    suspend fun getTrackComments(
        trackId: String
    ): ApiResponse<List<TrackResponse.GetTrackComment>> {
        return executeApiCall { trackApi.getTrackComments(trackId) }
    }

    // 트랙 댓글 작성
    suspend fun createTrackComment(
        request: TrackRequest.CreateCommentRequest
    ): ApiResponse<Int> {
        return executeApiCall { trackApi.createTrackComment(request) }
    }

    // 트랙 댓글 수정
    suspend fun updateTrackComment(
        request: TrackRequest.UpdateCommentRequest
    ): ApiResponse<Unit> {
        return executeApiCall { trackApi.updateTrackComment(request) }
    }

    // 트랙 댓글 삭제
    suspend fun deleteTrackComment(
        commentId: String
    ): ApiResponse<Unit> {
        return executeApiCall { trackApi.deleteTrackComment(commentId) }
    }

    // 트랙 검색
    suspend fun searchTracks(
        request: TrackRequest.SearchTrackRequest
    ): ApiResponse<List<TrackResponse.SearchTrack>> {
        return executeApiCall { trackApi.searchTracks(request) }
    }

    // 트랙 신고
    suspend fun reportTrack(
        request: TrackRequest.ReportTrackRequest
    ): ApiResponse<Unit> {
        return executeApiCall { trackApi.reportTrack(request) }
    }

    // 트랙 이미지 업로드
    suspend fun uploadTrackImage(
        trackId: Int,
        image: MultipartBody.Part
    ): ApiResponse<Unit> {
        val trackIdBody: RequestBody =
            trackId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        return executeApiCall { trackApi.uploadTrackImage(trackIdBody, image) }
    }

    companion object
}
