package com.whistlehub.playlist.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.whistlehub.common.data.remote.dto.response.TrackResponse
import com.whistlehub.common.data.repository.TrackService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class TrackPlayViewModel @Inject constructor(
    val exoPlayer: ExoPlayer,
    val trackService: TrackService,
) : ViewModel() {

    // 테스트용 트랙 리스트 (최종 API 연결 후 삭제)
    private val _trackList = MutableStateFlow<List<TrackResponse.GetTrackDetailResponse>>(listOf(
        TrackResponse.GetTrackDetailResponse(
            trackId = 1,
            title = "Sample Track 1",
            description = "Description 1",
            duration = 300,
            imageUrl = "https://picsum.photos/200/300?random=1",
            artistInfo = TrackResponse.ArtistInfo(1, "Artist 1", "https://picsum.photos/200/300?random=121"),
            isLike = false,
            importCount = 10,
            likeCount = 5,
            viewCount = 100,
            createdAt = "2023-01-01",
            sourceTrack = emptyList(),
            importTrack = emptyList(),
            tags = listOf("Pop", "Rock")
        ),
        TrackResponse.GetTrackDetailResponse(
            trackId = 2,
            title = "Sample Track 2",
            description = "Description 2",
            duration = 400,
            imageUrl = "https://picsum.photos/200/300?random=2",
            artistInfo = TrackResponse.ArtistInfo(2, "Artist 2", "https://picsum.photos/200/300?random=122"),
            isLike = true,
            importCount = 20,
            likeCount = 10,
            viewCount = 200,
            createdAt = "2023-02-01",
            sourceTrack = emptyList(),
            importTrack = emptyList(),
            tags = listOf("Jazz", "Blues")
        ),
        TrackResponse.GetTrackDetailResponse(
            trackId = 3,
            title = "Sample Track 3",
            description = "Description 3",
            duration = 500,
            imageUrl = "https://picsum.photos/200/300?random=3",
            artistInfo = TrackResponse.ArtistInfo(3, "Artist 3", "https://picsum.photos/200/300?random=123"),
            isLike = false,
            importCount = 30,
            likeCount = 15,
            viewCount = 300,
            createdAt = "2023-03-01",
            sourceTrack = emptyList(),
            importTrack = emptyList(),
            tags = listOf("Classical", "Orchestral")
        ),
    ))
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
                val mediaItem = MediaItem.fromUri(byteArrayToUri(this as Context, trackData)?: Uri.EMPTY )
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
