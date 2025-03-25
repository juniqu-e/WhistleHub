package com.whistlehub.playlist.viewmodel

import androidx.lifecycle.ViewModel
import com.whistlehub.common.data.remote.dto.response.PlaylistResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor() : ViewModel() {
    private val _playlists = MutableStateFlow<List<PlaylistResponse.GetMemberPlaylistsResponse>>(emptyList())
    val playlists: MutableStateFlow<List<PlaylistResponse.GetMemberPlaylistsResponse>> get() = _playlists

    private val _playlistInfo = MutableStateFlow<PlaylistResponse.GetPlaylistResponse?>(null)
    val playlistInfo: MutableStateFlow<PlaylistResponse.GetPlaylistResponse?> get() = _playlistInfo

    private val _playlistTrack = MutableStateFlow<List<PlaylistResponse.PlaylistTrackResponse>>(emptyList())
    val playlistTrack: MutableStateFlow<List<PlaylistResponse.PlaylistTrackResponse>> get() = _playlistTrack


    fun getPlaylists() {
        // 임시 리스트 생성
        _playlists.value = listOf(
            PlaylistResponse.GetMemberPlaylistsResponse(
                playlistId = 1,
                name = "My Playlist 1",
                imageUrl = "https://picsum.photos/200/300?random=51"
            ),
            PlaylistResponse.GetMemberPlaylistsResponse(
                playlistId = 2,
                name = "My Playlist 2",
                imageUrl = "https://picsum.photos/200/300?random=52"
            ),
            PlaylistResponse.GetMemberPlaylistsResponse(
                playlistId = 3,
                name = "My Playlist 3",
                imageUrl = "https://picsum.photos/200/300?random=53"
            ),
        )
    }

    fun getPlaylistTrack(playlistId: Int) {
        // 임시 플레이리스트 정보 생성
        _playlistInfo.value = PlaylistResponse.GetPlaylistResponse(
            memberId = 1,
            name = "My Playlist 1",
            description = "This is a sample playlist description.",
            imageUrl = "https://picsum.photos/200/300?random=61"
        )

        // 임시 리스트 생성 (ID 생략)
        _playlistTrack.value = listOf(
            PlaylistResponse.PlaylistTrackResponse(
                playlistTrackId = 1,
                playOrder = 1.0,
                trackInfo = PlaylistResponse.Track(
                    trackId = 1,
                    nickname = "Artist 1",
                    title = "Track 1",
                    duration = 180,
                    imageUrl = "https://picsum.photos/200/300?random=71"
                )
            ),
            PlaylistResponse.PlaylistTrackResponse(
                playlistTrackId = 2,
                playOrder = 2.0,
                trackInfo = PlaylistResponse.Track(
                    trackId = 2,
                    nickname = "Artist 1",
                    title = "Track 2",
                    duration = 240,
                    imageUrl = "https://picsum.photos/200/300?random=72"
                )
            ),
        )
    }
}