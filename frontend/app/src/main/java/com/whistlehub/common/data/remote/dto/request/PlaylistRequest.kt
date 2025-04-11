package com.whistlehub.common.data.remote.dto.request

/**
---------------------------
플레이리스트 관련 API 요청 DTO
---------------------------
 **/

sealed class PlaylistRequest {
    // 플레이리스트 생성
    data class CreatePlaylistRequest(
        val name: String,
        val description: String?,
        val trackIds: List<Int>?
    )
    // 플레이리스트 수정
    data class UpdatePlaylistRequest(
        val playlistId: Int,
        val name: String,
        val description: String,
    )
    // 플레이리스트에 트랙 추가
    data class AddTrackToPlaylistRequest(
        val playlistId: Int,
        val trackId: Int
    )
    // 플레이리스트 내부 수정 (위치이동, 삭제)
    data class UpdatePlaylistTrackRequest(
        val playlistId: Int,
        val tracks: List<Int>?
    )
}