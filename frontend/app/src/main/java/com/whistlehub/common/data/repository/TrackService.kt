package com.whistlehub.common.data.repository

import android.net.http.HttpException
import android.os.Build
import androidx.annotation.RequiresExtension
import com.whistlehub.common.data.remote.api.TrackApi
import com.whistlehub.common.data.remote.dto.request.TrackRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.TrackResponse
import com.whistlehub.common.util.TokenRefresh
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
---------------------------------------
트랙 관련 API 호출을 담당하는 서비스 클래스
---------------------------------------
 **/
@Singleton
class TrackService @Inject constructor(
    private val trackApi: TrackApi, private val tokenRefresh: TokenRefresh
) : ApiRepository() {
    // 트랙 상세 조회
    suspend fun getTrackDetail(
        token: String, trackId: String
    ): ApiResponse<TrackResponse.GetTrackDetailResponse> {
        return executeApiCall { trackApi.getTrackDetail(token, trackId) }
    }

    // 트랙 정보 수정
    suspend fun updateTrack(
        token: String, request: TrackRequest.UpdateTrackRequest
    ): ApiResponse<Unit> {
        return executeApiCall { trackApi.updateTrack(token, request) }
    }

    // 트랙 삭제
    suspend fun deleteTrack(
        token: String, trackId: String
    ): ApiResponse<Unit> {
        return executeApiCall { trackApi.deleteTrack(token, trackId) }
    }

    // 트랙 재생 요청
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    suspend fun playTrack(
        token: String,
        trackId: String,
    ): ByteArray? {
        return try {
            val response = trackApi.playTrack(token, trackId) // API 호출
            if (response.isSuccessful) {
                response.body()?.bytes() // 성공하면 ByteArray 반환
            } else {
                null // 실패 시 null 반환
            }
        } catch (e: HttpException) {
            e.printStackTrace()
            null // HTTP 오류 발생 시 null
        } catch (e: IOException) {
            e.printStackTrace()
            null // 네트워크 오류 발생 시 null
        }
    }

    // 트랙 재생 카운트 증가
    suspend fun increasePlayCount(
        token: String, request: TrackRequest.TrackPlayCountRequest
    ): ApiResponse<Unit> {
        return executeApiCall { trackApi.increasePlayCount(token, request) }
    }

    // 플레이리스트에 트랙 추가
    suspend fun addTrackToPlaylist(
        token: String, request: TrackRequest.AddTrackToPlaylistRequest
    ): ApiResponse<Unit> {
        return executeApiCall { trackApi.addTrackToPlaylist(token, request) }
    }

    // 트랙 레이어 조회
    suspend fun getTrackLayers(
        token: String, trackId: String
    ): ApiResponse<List<TrackResponse.GetTrackLayer>> {
        return executeApiCall { trackApi.getTrackLayers(token, trackId) }
    }

    // 트랙 레이어 재생
    suspend fun playLayer(
        token: String, layerId: String
    ): ApiResponse<TrackResponse.TrackLayerPlay> {
        return executeApiCall { trackApi.playLayer(token, layerId) }
    }

    // 트랙 좋아요 상태 조회
    suspend fun getTrackLikeStatus(
        token: String, trackId: String
    ): ApiResponse<Boolean> {
        return executeApiCall { trackApi.getTrackLikeStatus(token, trackId) }
    }

    // 트랙 좋아요
    suspend fun likeTrack(
        token: String, request: TrackRequest.LikeTrackRequest
    ): ApiResponse<Unit> {
        return executeApiCall { trackApi.likeTrack(token, request) }
    }

    // 트랙 댓글 조회
    suspend fun getTrackComments(
        token: String, trackId: String
    ): ApiResponse<List<TrackResponse.GetTrackComment>> {
        return executeApiCall { trackApi.getTrackComments(token, trackId) }
    }

    // 트랙 댓글 작성
    suspend fun createTrackComment(
        token: String, request: TrackRequest.CreateCommentRequest
    ): ApiResponse<Int> {
        return executeApiCall { trackApi.createTrackComment(token, request) }
    }

    // 트랙 댓글 수정
    suspend fun updateTrackComment(
        token: String, request: TrackRequest.UpdateCommentRequest
    ): ApiResponse<Unit> {
        return executeApiCall { trackApi.updateTrackComment(token, request) }
    }

    // 트랙 댓글 삭제
    suspend fun deleteTrackComment(
        token: String, commentId: String
    ): ApiResponse<Unit> {
        return executeApiCall { trackApi.deleteTrackComment(token, commentId) }
    }

    // 트랙 검색
    suspend fun searchTracks(
        token: String, request: TrackRequest.SearchTrackRequest
    ): ApiResponse<List<TrackResponse.SearchTrack>> {
        return executeApiCall { trackApi.searchTracks(token, request) }
    }

    // 트랙 신고
    suspend fun reportTrack(
        token: String, request: TrackRequest.ReportTrackRequest
    ): ApiResponse<Unit> {
        return executeApiCall { trackApi.reportTrack(token, request) }
    }

    // 트랙 이미지 업로드
    suspend fun uploadTrackImage(
        token: String, trackId: Int, image: MultipartBody.Part
    ): ApiResponse<Unit> {
        val trackIdBody: RequestBody =
            trackId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        return executeApiCall { trackApi.uploadTrackImage(token, trackIdBody, image) }
    }
}
