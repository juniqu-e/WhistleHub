package com.whistlehub.common.data.remote.dto.request

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

/**
---------------------------
워크스테이션 관련 API 요청 DTO
---------------------------
 **/
sealed class WorkstationRequest {
    // 트랙 업로드
    data class UploadTrackRequestRaw(
        val title: String,
        val description: String?,
        val duration: Int,
        val visibility: Boolean,
        val tags: List<String>,
        val sourceTracks: List<String>,
        val trackImg: File,
        val layerSoundFiles: List<File>,
        val layerName: List<String>,
        val trackSoundFile: File,
        val instrumentType: List<String>,
    )

    data class UploadTrackRequest(
        val partMap: HashMap<String, RequestBody>,
        val trackImg: MultipartBody.Part?,
        val layerSoundFiles: List<MultipartBody.Part>,
        val trackSoundFile: MultipartBody.Part,
    )

    // 불러온 레이어
    data class ImportedLayer(
        val layerId: Int,
        val modification: JsonObject
    )

    // 생성한 레이어
    data class NewLayer(
        val layerName: String,
        val instrumentType: String,
        val modification: JsonObject,
        val layerFileId: Int?
    )

    // 트랙 임포트
    data class ImportTrackRequest(
        val trackId: Int,
    )
//    data class ImportTrackRequest(
//        val trackId: Int,
//        val layerIds: List<Int>
//    )
}

