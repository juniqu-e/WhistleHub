package com.whistlehub.playlist.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.whistlehub.common.data.remote.dto.response.TrackResponse
import com.whistlehub.common.data.repository.TrackService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class TrackPlayViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val exoPlayer: ExoPlayer,
    val trackService: TrackService,
) : ViewModel() {

    // 테스트용 트랙 리스트 (최종 API 연결 후 삭제)
    private val _trackList = MutableStateFlow<List<TrackResponse.GetTrackDetailResponse>>(emptyList())
    val trackList: StateFlow<List<TrackResponse.GetTrackDetailResponse>> get() = _trackList

    // 현재 재생 중인 트랙
    private val _currentTrack = MutableStateFlow<TrackResponse.GetTrackDetailResponse?>(null)
    val currentTrack: StateFlow<TrackResponse.GetTrackDetailResponse?> get() = _currentTrack

    // 재생 상태 (재생 중/일시 정지)
    private val _isPlaying = MutableStateFlow<Boolean>(false)
    val isPlaying: StateFlow<Boolean> get() = _isPlaying

    // 플레이어 화면 상태
    private val _playerViewState = MutableStateFlow<PlayerViewState>(PlayerViewState.PLAYING)
    val playerViewState: StateFlow<PlayerViewState> get() = _playerViewState

    // 플레이어 내부 트랙 리스트
    private val _playerTrackList = MutableStateFlow<List<TrackResponse.GetTrackDetailResponse>>(emptyList())
    val playerTrackList: MutableStateFlow<List<TrackResponse.GetTrackDetailResponse>> get() = _playerTrackList

    // 현재 트랙 위치
    private val _playerPosition = MutableStateFlow(0L)
    val playerPosition: StateFlow<Long> get() = _playerPosition

    // 현재 트랙 길이
    private val _trackDuration = MutableStateFlow(0L)
    val trackDuration: StateFlow<Long> get() = _trackDuration

    init {
        // 트랙 리스트 추가
        viewModelScope.launch (Dispatchers.IO) {
            val trackRequests = (1 until 7).map { trackId ->
                async {
                    val resopnse = trackService.getTrackDetail(trackId.toString())
                    Log.d("TrackPlayViewModel", "트랙 ID: $trackId")
                    Log.d("TrackPlayViewModel", "트랙 응답: $resopnse")
                    resopnse
                } // 병렬 요청
            }

            val trackResults = trackRequests.awaitAll().mapNotNull { it.payload } // 모든 요청 완료 후 리스트 생성

            withContext(Dispatchers.Main) {
                _trackList.emit(trackResults)
            }
            Log.d("TrackPlayViewModel", "트랙 리스트: $trackResults")
            Log.d("TrackPlayViewModel", "갱신된 리스트: ${_trackList.value}")
        }
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

    fun playTrack(track: TrackResponse.GetTrackDetailResponse) {
        viewModelScope.launch {
            val trackData = trackService.playTrack(trackId = track.trackId.toString())

            if (trackData != null) {
                val mediaItem = MediaItem.fromUri(byteArrayToUri(context, trackData) ?: Uri.EMPTY)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.play()

                _currentTrack.value = track
                _isPlaying.value = true

                // 플레이어 트랙 리스트 업데이트
                val existingIndex = _playerTrackList.value.indexOfFirst { it.trackId == track.trackId }
                if (existingIndex == -1) {
                    _playerTrackList.value = _playerTrackList.value + track
                }
            }
        }
        val existingIndex = _playerTrackList.value.indexOfFirst { it.trackId == track.trackId }

        if (existingIndex == -1) { // 플레이어에 없는 경우 추가
            _playerTrackList.value = _playerTrackList.value + track
        }
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
        val currentIndex = _playerTrackList.value.indexOfFirst { it.trackId == _currentTrack.value?.trackId }
        if (currentIndex > 0) {
            playTrack(_playerTrackList.value[currentIndex - 1])
        } else {
//            stopTrack() // 첫 곡이면 정지
            playTrack(_playerTrackList.value.last()) // 첫 곡이면 마지막 곡으로 돌아감
        }
    }

    fun nextTrack() {
        val currentIndex = _playerTrackList.value.indexOfFirst { it.trackId == _currentTrack.value?.trackId }
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

fun byteArrayToUri(context: Context, byteArray: ByteArray): Uri? {
    // 임시 파일을 만들기 위한 파일 이름 지정
    val file = File(context.cacheDir, "temp_file")

    try {
        // 파일에 ByteArray를 씁니다.
        val fileOutputStream = FileOutputStream(file)
        fileOutputStream.write(byteArray)
        fileOutputStream.close()

        // 파일의 Uri를 반환합니다.
        return Uri.fromFile(file)
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }
}

enum class PlayerViewState {
    PLAYING,
    COMMENT,
    PLAYLIST,
    MORE
}
