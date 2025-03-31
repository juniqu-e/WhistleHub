package com.whistlehub.common.data.remote.dto.request

import com.google.gson.JsonObject

/**
---------------------------
워크스테이션 관련 API 요청 DTO
---------------------------
 **/

sealed class WorkstationRequest {
    // 트랙 업로드
    data class UploadTrackRequest(
        val title: String,
        val description: String?,
        val duration: Int,
        val visibility: Boolean,
        val tags: List<String>,
        val importedLayers: List<ImportedLayer>,
        val newLayers: List<NewLayer>
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
        val layerIds: List<Int>
    )
}