package com.whistlehub.common.data.remote.dto.response

/**
---------------------------
워크스테이션 관련 API 응답 DTO
---------------------------
 **/
sealed class WorkstationResponse {
    // 트랙 임포트
//    data class ImportTrackResponse(
//        val layerId: Int,
//        val modification: JsonObject,
//        val soundUrl: String?
//    )
    data class ImportTrackResponse(
        val layerId: Int,
        val title: String,
        val imageUrl: String?,
        val soundUrl: String,
        val layers: List<LayerResponse>
    )

    data class LayerResponse(
        val layerId: Int,
        val trackId: Int,
        val name: String,
        val instrumentType: Int,
        val soundUrl: String,
        val bars: List<Int>?,
        val bpm: Int?
    )
}