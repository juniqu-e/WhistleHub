package com.whistlehub.common.data.remote.dto.response

import com.google.gson.JsonObject

/**
---------------------------
워크스테이션 관련 API 응답 DTO
---------------------------
 **/

sealed class WorkstationResponse {
    // 트랙 임포트
    data class ImportTrackResponse(
        val layerId: Int,
        val modification: JsonObject,
        val soundUrl: String?
    )
}