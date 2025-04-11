package com.whistlehub.common.data.remote.api

import com.whistlehub.common.data.remote.dto.request.WorkstationRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.WorkstationResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Query

/**
----------------------------
워크스테이션 관련 API 인터페이스
----------------------------
 **/
interface WorkstationApi {
    // 트랙 업로드
    @Multipart
    @POST("workstation")
    suspend fun uploadTrack(
        @PartMap partMap: HashMap<String, RequestBody>,
        @Part trackImg: MultipartBody.Part?,
        @Part layerSoundFiles: List<MultipartBody.Part>,
        @Part trackSoundFile: MultipartBody.Part,
    ): Response<ApiResponse<Int>>

    // 레이어 업로드
    @Multipart
    @POST("workstation/layer")
    suspend fun uploadLayerFile(
        @Part file: MultipartBody.Part,
    ): Response<ApiResponse<Int>>

    // 트랙 임포트
    @GET("workstation/import")
    suspend fun importTrack(
        @Query("trackId") trackId: Int,
    ): Response<ApiResponse<WorkstationResponse.ImportTrackResponse>>

    // AI 추천 트랙 임포트
    @POST("workstation/ai/recommend")
    suspend fun importRecommendTrack(
        @Body layerIds: WorkstationRequest.ImportRecommendTrackRequest
    ): Response<ApiResponse<WorkstationResponse.ImportTrackResponse>>
    // 트랙 임포트
//    @GET("workstation/import")
//    suspend fun importTrack(
//        @Body request: WorkstationRequest.ImportTrackRequest
//    ): Response<ApiResponse<WorkstationResponse.ImportTrackResponse>>
}