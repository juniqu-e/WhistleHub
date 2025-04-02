package com.whistlehub.playlist.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whistlehub.common.data.local.room.UserRepository
import com.whistlehub.common.data.remote.dto.request.PlaylistRequest
import com.whistlehub.common.data.remote.dto.response.PlaylistResponse
import com.whistlehub.common.data.repository.PlaylistService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    val playlistService: PlaylistService,
    val userRepository: UserRepository
) : ViewModel() {
    private val _playlists = MutableStateFlow<List<PlaylistResponse.GetMemberPlaylistsResponse>>(emptyList())
    val playlists: StateFlow<List<PlaylistResponse.GetMemberPlaylistsResponse>> get() = _playlists

    private val _playlistInfo = MutableStateFlow<PlaylistResponse.GetPlaylistResponse?>(null)
    val playlistInfo: StateFlow<PlaylistResponse.GetPlaylistResponse?> get() = _playlistInfo

    private val _playlistTrack = MutableStateFlow<List<PlaylistResponse.PlaylistTrackResponse>>(emptyList())
    val playlistTrack: StateFlow<List<PlaylistResponse.PlaylistTrackResponse>> get() = _playlistTrack


    suspend fun getPlaylists() {
        try {
            val user = userRepository.getUser() // 사용자 정보 가져오기

            // 사용자 정보가 없을 경우 기본값으로 1(테스트계정 ID) 사용
            if (user == null) {
                Log.d("warning", "User not found, using default ID 1")
            }
            val playlistResponse = playlistService.getMemberPlaylists(user?.memberId ?: 0, 0, 10) // 페이지는 0번부터

            withContext(Dispatchers.Main) {
                _playlists.emit(playlistResponse.payload ?: emptyList())
            }
        } catch (e: Exception) {
            Log.d("error", "Failed to get playlists: ${e.message}")
        }
    }

    suspend fun getPlaylistInfo(playlistId: Int) {
        try {
            val playlistInfoResponse = playlistService.getPlaylists(playlistId)
            if (playlistInfoResponse.code == "SU") {
                _playlistInfo.value = playlistInfoResponse.payload
            } else {
                Log.d("error", "Failed to get playlist info: ${playlistInfoResponse.message}")
            }
        } catch (e: Exception) {
            Log.d("error", "Failed to get playlist info: ${e.message}")
        }
    }

    suspend fun getPlaylistTrack(playlistId: Int) {
        try {
            val playlistTrackResponse = playlistService.getPlaylistTracks(playlistId)
            if (playlistTrackResponse.code == "SU") {
                _playlistTrack.value = playlistTrackResponse.payload ?: emptyList()
            } else {
                Log.d("error", "Failed to get playlist tracks: ${playlistTrackResponse.message}")
            }
        } catch (e: Exception) {
            Log.d("error", "Failed to get playlist tracks: ${e.message}")
        }
    }

    fun moveTrack(from: Int, to: Int) {
        viewModelScope.launch {
            _playlistTrack.value = _playlistTrack.value.toMutableList().apply {
                add(to, removeAt(from))
            }
        }
    }

    suspend fun addTrackToPlaylist(playlistId: Int, trackId: Int) {
        try {
            val addTrackResponse = playlistService.addTrackToPlaylist(
                PlaylistRequest.AddTrackToPlaylistRequest(
                    playlistId = playlistId,
                    trackId = trackId
                )
            )
            if (addTrackResponse.code == "SU") {
                Log.d("success", "Track added to playlist successfully Track$trackId into playlist$playlistId")
            } else {
                Log.d("error", "${addTrackResponse.message}")
            }
        } catch (e: Exception) {
            Log.d("error", "Failed to add track to playlist: ${e.message}")
        }
    }

    suspend fun createPlaylist(
        name: String = "New Playlist",
        description: String? = null,
        trackIds: List<Int>? = null,
        image: MultipartBody.Part? = null
    ) {
        try {
            val createPlaylistResponse = playlistService.createPlaylist(
                name = name,
                description = description,
                trackIds = trackIds,
                image = image
            )
            if (createPlaylistResponse.code == "SU") {
                Log.d("success", "Playlist created successfully with ID ${createPlaylistResponse.payload}")
                getPlaylists() // 플레이리스트 목록 갱신
            } else {
                Log.d("error", "${createPlaylistResponse.message}")
            }
        } catch (e: Exception) {
            Log.d("error", "Failed to create playlist: ${e.message}")
        }
    }

    suspend fun deletePlaylist(playlistId: Int) {
        try {
            val deletePlaylistResponse = playlistService.deletePlaylist(playlistId)
            if (deletePlaylistResponse.code == "SU") {
                Log.d("success", "Playlist deleted successfully with ID $playlistId")
                getPlaylists() // 플레이리스트 목록 갱신
            } else {
                Log.d("error", "${deletePlaylistResponse.message}")
            }
        } catch (e: Exception) {
            Log.d("error", "Failed to delete playlist: ${e.message}")
        }
    }

    suspend fun updatePlaylist(
        playlistId: Int,
        name: String,
        description: String,
        trackIds: List<Int> = emptyList(),
    ) {
        try {
            val updatePlaylistResponse = playlistService.updatePlaylist(
                request = PlaylistRequest.UpdatePlaylistRequest(
                    playlistId = playlistId,
                    name = name,
                    description = description
                )
            )
            if (trackIds.isNotEmpty()) {
                playlistService.updatePlaylistTracks(
                    request = PlaylistRequest.UpdatePlaylistTrackRequest(
                        playlistId = playlistId,
                        tracks = trackIds
                    )
                )
            }
            if (updatePlaylistResponse.code == "SU") {
                Log.d("success", "Playlist updated successfully with ID $playlistId")
                getPlaylistInfo(playlistId)
                getPlaylistTrack(playlistId)
            } else {
                Log.d("error", "${updatePlaylistResponse.message}")
            }
        } catch (e: Exception) {
            Log.d("error", "Failed to update playlist: ${e.message}")
        }
    }
}