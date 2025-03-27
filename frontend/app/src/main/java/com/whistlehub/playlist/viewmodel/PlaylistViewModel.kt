package com.whistlehub.playlist.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whistlehub.common.data.local.room.UserRepository
import com.whistlehub.common.data.remote.dto.response.PlaylistResponse
import com.whistlehub.common.data.repository.PlaylistService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    val playlistService: PlaylistService,
    val userRepository: UserRepository
) : ViewModel() {
    private val _playlists = MutableStateFlow<List<PlaylistResponse.GetMemberPlaylistsResponse>>(emptyList())
    val playlists: MutableStateFlow<List<PlaylistResponse.GetMemberPlaylistsResponse>> get() = _playlists

    private val _playlistInfo = MutableStateFlow<PlaylistResponse.GetPlaylistResponse?>(null)
    val playlistInfo: MutableStateFlow<PlaylistResponse.GetPlaylistResponse?> get() = _playlistInfo

    private val _playlistTrack = MutableStateFlow<List<PlaylistResponse.PlaylistTrackResponse>>(emptyList())
    val playlistTrack: MutableStateFlow<List<PlaylistResponse.PlaylistTrackResponse>> get() = _playlistTrack


    fun getPlaylists() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = userRepository.getUser() // 사용자 정보 가져오기

            // 사용자 정보가 없을 경우 기본값으로 1(테스트계정 ID) 사용
            if (user == null) {
                Log.d("warning", "User not found, using default ID 1")
            }
            val playlistResponse = playlistService.getMemberPlaylists(user?.memberId ?: 1, 1, 10)

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
}