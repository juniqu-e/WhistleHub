package com.whistlehub.common.data.remote.api

import retrofit2.Response
import retrofit2.http.*

/**
 플레이리스트 관련 API 요청을 정의하는 인터페이스
 **/
interface PlaylistApi {
    // 플레이리스트 목록 조회
    @GET("/playlist")
    suspend fun getPlaylist(): Response<Any>
    // 플레이리스트 생성
    @POST("/playlist")
    suspend fun createPlaylist(@Body requestBody: Any): Response<Any>
    // 플레이리스트 정보 수정
    @PUT("/playlist")
    suspend fun updatePlaylist(@Body requestBody: Any): Response<Any>
    // 플레이리스트 삭제
    @DELETE("/playlist")
    suspend fun deletePlaylist(@Query("playlistId") playlistId: String): Response<Any> 
    // 플레이리스트 상세 조회
    @GET("/playlist/track")
    suspend fun getPlaylistTrack(@Query("playlistId") playlistId: String): Response<Any>
    // 플레이리스트 트랙 위치 변경 및 삭제
    @PUT("/playlist/track")
    suspend fun updatePlaylistTrack(@Body requestBody: Any): Response<Any>
    // 플레이리스트 상세 조회 이미지 업로드
    @POST("/playlist/image")
    suspend fun uploadPlaylistImage(@Query("playlistId") playlistId: String): Response<Any>
}