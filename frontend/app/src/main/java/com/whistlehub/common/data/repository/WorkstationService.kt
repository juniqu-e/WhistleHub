package com.whistlehub.common.data.repository

import com.whistlehub.common.data.remote.api.WorkstationApi
import com.whistlehub.common.data.remote.dto.request.WorkstationRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.WorkstationResponse
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

/**
---------------------------------------------
워크스테이션 관련 API 호출을 담당하는 서비스 클래스
---------------------------------------------
 **/

@Singleton
class WorkstationService @Inject constructor(
    private val workstationApi: WorkstationApi,
) : ApiRepository() {

    // 트랙 업로드
    suspend fun uploadTrack(
        request: WorkstationRequest.UploadTrackRequest
    ): ApiResponse<Int> {
        return executeApiCall { workstationApi.uploadTrack(request) }
    }

    // 레이어 업로드
    suspend fun uploadLayerFile(
        file: MultipartBody.Part
    ): ApiResponse<Int> {
        return executeApiCall { workstationApi.uploadLayerFile(file) }
    }

    // 트랙 임포트
    suspend fun importTrack(
        trackId: Int,
        layerIds: List<Int>
    ): ApiResponse<WorkstationResponse.ImportTrackResponse> {
        val request = WorkstationRequest.ImportTrackRequest(trackId, layerIds)
        return executeApiCall { workstationApi.importTrack(request) }
    }
}