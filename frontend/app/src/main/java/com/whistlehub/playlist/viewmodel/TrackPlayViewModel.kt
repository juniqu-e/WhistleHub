package com.whistlehub.playlist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.whistlehub.playlist.data.Track
import com.whistlehub.playlist.data.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class TrackPlayViewModel @Inject constructor(
    private val exoPlayer: ExoPlayer,
    private val trackRepository: TrackRepository
) : ViewModel() {

    // 트랙 리스트
    private val _trackList = MutableStateFlow<List<Track>>(emptyList())
    val trackList: StateFlow<List<Track>> get() = _trackList

    // 현재 재생 중인 트랙
    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> get() = _currentTrack

    // 재생 상태 (재생 중/일시 정지)
    private val _isPlaying = MutableStateFlow<Boolean>(false)
    val isPlaying: StateFlow<Boolean> get() = _isPlaying

    // 트랙 목록 로드
    fun loadTracks() {
        _trackList.value = trackRepository.getTracks()
    }

    // 트랙 재생
    fun playTrack(track: Track) {
        _currentTrack.value = track
        val mediaItem = MediaItem.fromUri(track.uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
        _isPlaying.value = true
    }

    // 트랙 일시 정지
    fun pauseTrack() {
        exoPlayer.pause()
        _isPlaying.value = false
    }

    // 트랙 정지
    fun stopTrack() {
        _currentTrack.value = null
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        _isPlaying.value = false
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }
}
