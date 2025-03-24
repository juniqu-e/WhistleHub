package com.whistlehub.playlist.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.whistlehub.playlist.data.Track
import com.whistlehub.playlist.data.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class TrackPlayViewModel @Inject constructor(
    val exoPlayer: ExoPlayer,
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

    // 플레이어 화면 상태
    private val _playerViewState = MutableStateFlow<PlayerViewState>(PlayerViewState.PLAYING)
    val playerViewState: StateFlow<PlayerViewState> get() = _playerViewState

    // 트랙 목록 로드
    fun loadTracks() {
        _trackList.value = trackRepository.getTracks()
    }

    // 플레이어 내부 트랙 리스트
    private val _playerTrackList = MutableStateFlow<List<Track>>(emptyList())
    val playerTrackList: MutableStateFlow<List<Track>> get() = _playerTrackList

    // 현재 트랙 위치
    private val _playerPosition = MutableStateFlow(0L)
    val playerPosition: StateFlow<Long> get() = _playerPosition

    // 현재 트랙 길이
    private val _trackDuration = MutableStateFlow(0L)
    val trackDuration: StateFlow<Long> get() = _trackDuration

    init {
    // ExoPlayer 이벤트 리스너 추가
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    _playerPosition.value = 0L
                    _trackDuration.value = 0L
                    nextTrack() // 트랙이 끝나면 다음 곡 자동 재생
                }
            }
        })
        viewModelScope.launch {
            while (true) {
                _playerPosition.value = exoPlayer.currentPosition
                _trackDuration.value = if (exoPlayer.duration != C.TIME_UNSET) exoPlayer.duration else 0L
                delay(1000)
            }
        }
    }

    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
    }

    fun playTrack(track: Track) {
        val existingIndex = _playerTrackList.value.indexOfFirst { it.id == track.id }

        if (existingIndex == -1) { // 플레이어에 없는 경우 추가
            _playerTrackList.value = _playerTrackList.value + track
        }

        _currentTrack.value = track
        _playerPosition.value = 0L
        _trackDuration.value = exoPlayer.duration
        val mediaItem = MediaItem.fromUri(track.uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
        _isPlaying.value = true
    }

    // 트랙 일시 정지
    fun pauseTrack() {
        exoPlayer.playWhenReady = false
        exoPlayer.pause()
        _isPlaying.value = false
    }

    // 트랙 재개
    fun resumeTrack() {
        exoPlayer.playWhenReady = true
        exoPlayer.play()
        _isPlaying.value = true
    }

    // 트랙 정지
    fun stopTrack() {
        _currentTrack.value = null
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        _isPlaying.value = false
    }

    fun previousTrack() {
        val currentIndex = _playerTrackList.value.indexOfFirst { it.id == _currentTrack.value?.id }
        if (currentIndex > 0) {
            playTrack(_playerTrackList.value[currentIndex - 1])
        } else {
//            stopTrack() // 첫 곡이면 정지
            playTrack(_playerTrackList.value.last()) // 첫 곡이면 마지막 곡으로 돌아감
        }
    }

    fun nextTrack() {
        val currentIndex = _playerTrackList.value.indexOfFirst { it.id == _currentTrack.value?.id }
        if (currentIndex != -1 && currentIndex < _playerTrackList.value.size - 1) {
            playTrack(_playerTrackList.value[currentIndex + 1])
        } else {
//            stopTrack() // 마지막 곡이면 정지
            playTrack(_playerTrackList.value[0]) // 마지막 곡이면 첫 곡으로 돌아감
        }
    }

    fun setPlayerViewState(state: PlayerViewState) {
        _playerViewState.value = state
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }
}

enum class PlayerViewState {
    PLAYING,
    COMMENT,
    PLAYLIST,
    MORE
}
