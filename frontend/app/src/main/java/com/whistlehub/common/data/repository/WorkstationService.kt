package com.whistlehub.common.data.repository

import com.whistlehub.common.data.remote.api.WorkstationApi
import com.whistlehub.common.data.remote.dto.request.WorkstationRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.WorkstationResponse
import com.whistlehub.common.util.TokenRefresh
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
    private val tokenRefresh: TokenRefresh
) : ApiRepository() {
    // 트랙 업로드
    suspend fun uploadTrack(
        request: WorkstationRequest.UploadTrackRequest
    ): ApiResponse<Int> {
        return tokenRefresh.execute {
            workstationApi.uploadTrack(
                partMap = request.partMap,
                trackImg = request.trackImg,
                layerSoundFiles = request.layerSoundFiles,
                trackSoundFile = request.trackSoundFile,
            )
        }
    }

    // 레이어 업로드
    suspend fun uploadLayerFile(
        file: MultipartBody.Part
    ): ApiResponse<Int> {
        return tokenRefresh.execute { workstationApi.uploadLayerFile(file) }
    }

    // 트랙 임포트
    suspend fun importTrack(
        request: WorkstationRequest.ImportTrackRequest
    ): ApiResponse<WorkstationResponse.ImportTrackResponse> {
        return tokenRefresh.execute {
            workstationApi.importTrack(
                trackId = request.trackId
            )
        }
    }
}