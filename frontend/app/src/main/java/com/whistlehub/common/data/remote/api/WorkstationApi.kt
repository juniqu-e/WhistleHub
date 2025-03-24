package com.whistlehub.common.data.remote.api

import com.whistlehub.common.data.remote.dto.request.WorkstationRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.WorkstationResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
----------------------------
워크스테이션 관련 API 인터페이스
----------------------------
 **/

interface WorkstationApi {
    // 트랙 업로드
    @POST("/workstation")
    suspend fun uploadTrack(
        @Header("Authorization") token: String,
        @Body request: WorkstationRequest.UploadTrackRequest
    ): Response<ApiResponse<Int>>
    // 레이어 업로드
    @Multipart
    @POST("/workstation/layer")
    suspend fun uploadLayerFile(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
    ): Response<ApiResponse<Int>>
    // 트랙 임포트
    @POST("/workstation/import")
    suspend fun importTrack(
        @Header("Authorization") token: String,
        @Body request: WorkstationRequest.ImportTrackRequest
    ): Response<ApiResponse<WorkstationResponse.ImportTrackResponse>>
}