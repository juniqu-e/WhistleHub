package com.whistlehub.playlist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val playlistService: PlaylistService
) : ViewModel() {
    private val _playlists = MutableStateFlow<List<PlaylistResponse.GetMemberPlaylistsResponse>>(emptyList())
    val playlists: MutableStateFlow<List<PlaylistResponse.GetMemberPlaylistsResponse>> get() = _playlists

    private val _playlistInfo = MutableStateFlow<PlaylistResponse.GetPlaylistResponse?>(null)
    val playlistInfo: MutableStateFlow<PlaylistResponse.GetPlaylistResponse?> get() = _playlistInfo

    private val _playlistTrack = MutableStateFlow<List<PlaylistResponse.PlaylistTrackResponse>>(emptyList())
    val playlistTrack: MutableStateFlow<List<PlaylistResponse.PlaylistTrackResponse>> get() = _playlistTrack


    fun getPlaylists() {
        viewModelScope.launch(Dispatchers.IO) {
            val playlistResponse = playlistService.getMemberPlaylists(1, 1, 1) // 모든 요청 완료 후 리스트 생성

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