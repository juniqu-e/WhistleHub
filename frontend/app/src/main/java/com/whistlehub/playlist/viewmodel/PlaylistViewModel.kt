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

    fun getPlaylistInfo(playlistId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val playlistResponse = playlistService.getPlaylists(playlistId) // 모든 요청 완료 후 리스트 생성

            withContext(Dispatchers.Main) {
                _playlistInfo.emit(playlistResponse.payload)
            }
        }
    }

    fun getPlaylistTrack(playlistId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val playlistTrackResponse =
                playlistService.getPlaylistTracks(playlistId) // 모든 요청 완료 후 리스트 생성

            withContext(Dispatchers.Main) {
                _playlistTrack.emit(playlistTrackResponse.payload ?: emptyList())
            }
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