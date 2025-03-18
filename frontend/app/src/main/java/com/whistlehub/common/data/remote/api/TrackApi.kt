package com.whistlehub.common.data.remote.api

import com.whistlehub.common.data.remote.dto.request.TrackRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.TrackResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
-----------------------
트랙 관련 API 인터페이스
-----------------------
 **/

interface TrackApi {
    // 트랙 상세 조회
    @GET("/track")
    suspend fun getTrackDetail(
        @Header("Authorization") token: String,
        @Query("trackId") trackId: String
    ): Response<ApiResponse<TrackResponse.GetTrackDetailResponse>>
    // 트랙 정보 수정
    @PUT("/track")
    suspend fun updateTrack(
        @Header("Authorization") token: String,
        @Body request: TrackRequest.UpdateTrackRequest)
    : Response<ApiResponse<Unit>>
    // 트랙 삭제
    @DELETE("/track")
    suspend fun deleteTrack(
        @Header("Authorization") token: String,
        @Query("trackId") trackId: String
    ): Response<ApiResponse<Unit>>
    // 트랙 재생 요청
    @GET("/track/play")
    suspend fun playTrack(
        @Header("Authorization") token: String,
        @Query("trackId") trackId: String
    ): Response<ApiResponse<String>>
    // 트랙 재생 카운트
    @POST("/track/play")
    suspend fun increasePlayCount(
        @Header("Authorization") token: String,
        @Body request: TrackRequest.TrackPlayCountRequest
    ): Response<ApiResponse<Unit>>
    // 플레이리스트에 트랙 추가
    @POST("/track/playlist")
    suspend fun addTrackToPlaylist(
        @Header("Authorization") token: String,
        @Body request: TrackRequest.AddTrackToPlaylistRequest
    ): Response<ApiResponse<Unit>>
    // 트랙 레이어 조회
    @GET("/track/layer")
    suspend fun getTrackLayers(
        @Header("Authorization") token: String,
        @Query("trackId") trackId: String
    ): Response<ApiResponse<List<TrackResponse.GetTrackLayer>>>
    // 트랙 레이어 재생
    @GET("/track/layer/play")
    suspend fun playLayer(
        @Header("Authorization") token: String,
        @Query("layerId") layerId: String
    ): Response<ApiResponse<TrackResponse.TrackLayerPlay>>
    // 트랙 좋아요 상태 조회
    @GET("/track/like")
    suspend fun getTrackLikeStatus(
        @Header("Authorization") token: String,
        @Query("trackId") trackId: String
    ): Response<ApiResponse<Boolean>>
    // 트랙 좋아요
    @POST("/track/like")
    suspend fun likeTrack(
        @Header("Authorization") token: String,
        @Body request: TrackRequest.LikeTrackRequest
    ): Response<ApiResponse<Unit>>
    // 트랙 댓글 조회
    @GET("/track/comment")
    suspend fun getTrackComments(
        @Header("Authorization") token: String,
        @Query("trackId") trackId: String
    ): Response<ApiResponse<List<TrackResponse.GetTrackComment>>>
    // 트랙 댓글 작성
    @POST("/track/comment")
    suspend fun createTrackComment(
        @Header("Authorization") token: String,
        @Body request: TrackRequest.CreateCommentRequest
    ): Response<ApiResponse<Int>>
    // 트랙 댓글 수정
    @PUT("/track/comment")
    suspend fun updateTrackComment(
        @Header("Authorization") token: String,
        @Body request: TrackRequest.UpdateCommentRequest
    ): Response<ApiResponse<Unit>>
    // 트랙 댓글 삭제
    @DELETE("/track/comment")
    suspend fun deleteTrackComment(
        @Header("Authorization") token: String,
        @Query("commentId") commentId: String
    ): Response<ApiResponse<Unit>>
    // 트랙 검색
    @POST("/track/search")
    suspend fun searchTracks(
        @Header("Authorization") token: String,
        @Body request: TrackRequest.SearchTrackRequest
    ): Response<ApiResponse<List<TrackResponse.SearchTrack>>>
    // 트랙 신고
    @POST("/track/report")
    suspend fun reportTrack(
        @Header("Authorization") token: String,
        @Body request: TrackRequest.ReportTrackRequest
    ): Response<ApiResponse<Unit>>
    // 트랙 이미지 업로드
    @Multipart
    @POST("/track/image")
    suspend fun uploadTrackImage(
        @Header("Authorization") token: String,
        @Part("trackId") trackId: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<ApiResponse<Unit>>
}
