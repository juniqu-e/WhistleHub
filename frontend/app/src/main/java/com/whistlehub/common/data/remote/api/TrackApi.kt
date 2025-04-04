package com.whistlehub.common.data.remote.api

import com.whistlehub.common.data.remote.dto.request.TrackRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.AuthResponse
import com.whistlehub.common.data.remote.dto.response.TrackResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
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
----------------------
트랙 관련 API 인터페이스
----------------------
 **/
interface TrackApi {
    // 트랙 상세 조회
    @GET("track")
    suspend fun getTrackDetail(
        @Query("trackId") trackId: String
    ): Response<ApiResponse<TrackResponse.GetTrackDetailResponse>>

    // 트랙 정보 수정
    @PUT("track")
    suspend fun updateTrack(
        @Body request: TrackRequest.UpdateTrackRequest
    )
            : Response<ApiResponse<Unit>>

    // 트랙 삭제
    @DELETE("track")
    suspend fun deleteTrack(
        @Query("trackId") trackId: String
    ): Response<ApiResponse<Unit>>

    // 트랙 재생 요청
    // 공통API 사용하지 않고 파일만 수신
    @GET("track/play")
    suspend fun playTrack(
        @Query("trackId") trackId: String
    ): Response<ResponseBody>

    // 트랙 재생 카운트
    @POST("track/play")
    suspend fun increasePlayCount(
        @Body request: TrackRequest.TrackPlayCountRequest
    ): Response<ApiResponse<Unit>>

    // 플레이리스트에 트랙 추가
    @POST("track/playlist")
    suspend fun addTrackToPlaylist(
        @Body request: TrackRequest.AddTrackToPlaylistRequest
    ): Response<ApiResponse<Unit>>

    // 트랙 레이어 조회
    @GET("track/layer")
    suspend fun getTrackLayers(
        @Query("trackId") trackId: String
    ): Response<ApiResponse<List<TrackResponse.GetTrackLayer>>>

    // 트랙 레이어 재생
    @GET("track/layer/play")
    suspend fun playLayer(
        @Query("layerId") layerId: String
    ): Response<ApiResponse<TrackResponse.TrackLayerPlay>>

    // 트랙 좋아요
    @POST("track/like")
    suspend fun likeTrack(
        @Body request: TrackRequest.LikeTrackRequest
    ): Response<ApiResponse<Unit>>

    // 트랙 좋아요 취소
    @DELETE("track/like")
    suspend fun unlikeTrack(
        @Query("trackId") trackId: String
    ): Response<ApiResponse<Unit>>

    // 트랙 댓글 조회
    @GET("track/comment")
    suspend fun getTrackComments(
        @Query("trackId") trackId: String
    ): Response<ApiResponse<List<TrackResponse.GetTrackComment>>>

    // 트랙 댓글 작성
    @POST("track/comment")
    suspend fun createTrackComment(
        @Body request: TrackRequest.CreateCommentRequest
    ): Response<ApiResponse<Int>>

    // 트랙 댓글 수정
    @PUT("track/comment")
    suspend fun updateTrackComment(
        @Body request: TrackRequest.UpdateCommentRequest
    ): Response<ApiResponse<Unit>>

    // 트랙 댓글 삭제
    @DELETE("track/comment")
    suspend fun deleteTrackComment(
        @Query("commentId") commentId: String
    ): Response<ApiResponse<Unit>>

    // 트랙 검색
    @GET("track/search")
    suspend fun searchTracks(
        @Query("keyword") keyword: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("orderBy") orderBy: String,
    ): Response<ApiResponse<List<TrackResponse.SearchTrack>>>

//    @POST("track/search")
//    suspend fun searchTracks(
//        @Body request: TrackRequest.SearchTrackRequest
//    ): Response<ApiResponse<List<TrackResponse.SearchTrack>>>

    // 트랙 신고
    @POST("track/report")
    suspend fun reportTrack(
        @Body request: TrackRequest.ReportTrackRequest
    ): Response<ApiResponse<Unit>>

    // 트랙 이미지 업로드
    @Multipart
    @POST("track/image")
    suspend fun uploadTrackImage(
        @Part("trackId") trackId: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<ApiResponse<String>>


    // 태그 추천
    @GET("discovery/tag")
    suspend fun getTagRecommendation(
    ): Response<ApiResponse<List<AuthResponse.TagResponse>>>

    // 태그 랭킹
    @GET("discovery/tag/ranking")
    suspend fun getTagRanking(
        @Query("tagId") tagId: Int,
        @Query("period") period: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): Response<ApiResponse<List<TrackResponse.SearchTrack>>>

    //태그별 추천 트랙
    @GET("discovery/tag/recommend")
    suspend fun getTagRecommendTrack(
        @Query("tagId") tagId: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<List<TrackResponse.SearchTrack>>>
}
