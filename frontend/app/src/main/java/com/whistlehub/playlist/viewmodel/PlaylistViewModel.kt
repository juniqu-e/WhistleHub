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


    fun getPlaylists() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = userRepository.getUser() // 사용자 정보 가져오기

            // 사용자 정보가 없을 경우 기본값으로 1(테스트계정 ID) 사용
            if (user == null) {
                Log.d("warning", "User not found, using default ID 1")
            }
            val playlistResponse = playlistService.getMemberPlaylists(user?.memberId ?: 1, 0, 10) // 페이지는 0번부터

            withContext(Dispatchers.Main) {
                _playlists.emit(playlistResponse.payload ?: emptyList())
            }
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

    fun addTrackToPlaylist(playlistId: Int, trackId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
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
        }
    }

    fun createPlaylist(
        name: String = "New Playlist",
        description: String? = null,
        trackIds: List<Int>? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val createPlaylistResponse = playlistService.createPlaylist(
                name = name,
                description = description,
                trackIds = trackIds
            )
            if (createPlaylistResponse.code == "SU") {
                Log.d("success", "Playlist created successfully with ID ${createPlaylistResponse.payload}")
                getPlaylists() // 플레이리스트 목록 갱신
            } else {
                Log.d("error", "${createPlaylistResponse.message}")
            }
        }
    }
}