package com.whistlehub.common.data.repository

// 플레이리스트
interface PlaylistRepository {
    suspend fun getPlaylist(): Result<Any>
    suspend fun createPlaylist(requestBody: Any): Result<Any>
    suspend fun updatePlaylist(requestBody: Any): Result<Any>
    suspend fun deletePlaylist(playlistId: String): Result<Any>
    suspend fun getPlaylistTrack(playlistId: String): Result<Any>
    suspend fun updatePlaylistTrack(requestBody: Any): Result<Any>
    suspend fun uploadPlaylistImage(playlistId: String): Result<Any>
}