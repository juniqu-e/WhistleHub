package com.whistlehub.playlist.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.whistlehub.common.data.local.entity.UserEntity
import com.whistlehub.common.data.local.room.UserRepository
import com.whistlehub.common.data.remote.dto.request.TrackRequest
import com.whistlehub.common.data.remote.dto.response.TrackResponse
import com.whistlehub.common.data.repository.TrackService
import com.whistlehub.playlist.data.TrackEssential
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
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.timer

@HiltViewModel
class TrackPlayViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val exoPlayer: ExoPlayer,
    val trackService: TrackService,
    val userRpository: UserRepository,
) : ViewModel() {
    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> get() = _user

    // 테스트용 트랙 리스트 (최종 API 연결 후 삭제)
    private val _trackList =
        MutableStateFlow<List<TrackEssential>>(emptyList())
    val trackList: StateFlow<List<TrackEssential>> get() = _trackList

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
    private val _playerTrackList =
        MutableStateFlow<List<TrackEssential>>(emptyList())
    val playerTrackList: MutableStateFlow<List<TrackEssential>> get() = _playerTrackList

    // 현재 트랙 위치
    private val _playerPosition = MutableStateFlow(0L)
    val playerPosition: StateFlow<Long> get() = _playerPosition

    // 현재 트랙 길이
    private val _trackDuration = MutableStateFlow(0L)
    val trackDuration: StateFlow<Long> get() = _trackDuration

    // 트랙 로그 기준 시간
    private val _LOG_TIME = 15L

    init {
        // 빌드 할 때 트랙 리스트 추가, 유저정보 파싱
        viewModelScope.launch(Dispatchers.IO) {
            // 유저 정보 가져오기
            _user.value = userRpository.getUser()
            // 임시 트랙 리스트 가져오기
            getTempTrackList()
        }
        // ExoPlayer 이벤트 리스너 추가
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    _playerPosition.value = 0L
                    _trackDuration.value = 0L
                    viewModelScope.launch {
                        nextTrack() // 트랙이 끝나면 다음 곡 자동 재생
                    }
                }
                when (state) {
                    Player.STATE_IDLE -> Log.d("ExoPlayer", "IDLE 상태")
                    Player.STATE_BUFFERING -> Log.d("ExoPlayer", "버퍼링 중")
                    Player.STATE_READY -> Log.d("ExoPlayer", "준비 완료, 재생 가능")
                    Player.STATE_ENDED -> Log.d("ExoPlayer", "재생 완료")
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("ExoPlayer", "재생 오류 발생: ${error.message}")
            }
        })
        viewModelScope.launch {
            while (true) {
                _playerPosition.value = exoPlayer.currentPosition
                _trackDuration.value =
                    if (exoPlayer.duration != C.TIME_UNSET) exoPlayer.duration else 0L
                delay(1000)
            }
        }
    }

    suspend fun getTempTrackList() {
        try {
            viewModelScope.launch(Dispatchers.IO) {
                val trackRequests = (1 until 7).map { trackId ->
                    async {
                        val resopnse = trackService.getTrackDetail(trackId.toString()).payload
                        if (resopnse == null) {
                            Log.d("TrackPlayViewModel", "트랙 정보가 없습니다.")
                            return@async null
                        }
                        val track = TrackEssential(
                            trackId = resopnse.trackId,
                            title = resopnse.title,
                            artist = resopnse.artist?.nickname ?: "Unknown Artist",
                            imageUrl = resopnse.imageUrl
                        )
                        track
                    } // 병렬 요청
                }
                val trackResults =
                    trackRequests.awaitAll().mapNotNull { it } // 모든 요청 완료 후 리스트 생성

                withContext(Dispatchers.Main) {
                    _trackList.emit(trackResults)
                }
                Log.d("TrackPlayViewModel", "트랙 리스트: $trackResults")
                Log.d("TrackPlayViewModel", "갱신된 리스트: ${_trackList.value}")
            }
        } catch (e: Exception) {
            Log.d("TrackPlayViewModel", "Error fetching track list: ${e.message}")
        }
    }

    suspend fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
    }

    suspend fun getTrackbyTrackId(trackId: Int): TrackResponse.GetTrackDetailResponse? {
        return try {
            val response = trackService.getTrackDetail(trackId.toString())
            if (response.code == "SU") {
                response.payload
            } else {
                Log.d("TrackPlayViewModel", "Failed to get track detail: ${response.message}")
                null
            }
        } catch (e: Exception) {
            Log.d("TrackPlayViewModel", "Error fetching track detail: ${e.message}")
            null
        }
    }

    suspend fun playTrack(trackId: Int) {
        try {
            val trackResponse = trackService.getTrackDetail(trackId.toString())  // 트랙 정보 가져오기
            if (trackResponse.code != "SU") {
                Log.d("TrackPlayViewModel", "Failed to get track detail: ${trackResponse.message}")
                stopTrack()
                return  // 트랙 정보 가져오기 실패 시 플레이어 종료
            }
            val track = trackResponse.payload ?: return  // 트랙 정보가 없으면 종료
            resetTimer() // 기존 타이머 초기화
            val trackData = trackService.playTrack(trackId.toString())

            if (trackData != null) {
                val mediaItem = MediaItem.fromUri(byteArrayToUri(context, trackData) ?: Uri.EMPTY)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
                exoPlayer.play()

                _currentTrack.value = track
                _isPlaying.value = true
                getTrackComment(trackId.toString())  // 댓글 정보 업데이트
                startTimer() // 타이머 시작

                // 플레이어 트랙 리스트 업데이트
                val existingIndex = _playerTrackList.value.indexOfFirst { it.trackId == trackId }
                if (existingIndex == -1) {
                    _playerTrackList.value += TrackEssential(
                        trackId = track.trackId,
                        title = track.title,
                        artist = track.artist?.nickname ?: "Unknown Artist",
                        imageUrl = track.imageUrl
                    )  // 현재 재생 목록 맨 뒤에 트랙 추가
                }
            }
        } catch (e: Exception) {
            Log.e("TrackPlayViewModel", "Error playing track: ${e.message}")
        }
    }

    // 트랙 일시 정지
    fun pauseTrack() {
        exoPlayer.playWhenReady = false
        exoPlayer.pause()
        _isPlaying.value = false
        pauseTimer() // 타이머 일시 정지
    }

    // 트랙 재개
    fun resumeTrack() {
        exoPlayer.playWhenReady = true
        exoPlayer.play()
        _isPlaying.value = true
        startTimer() // 타이머 재개
    }

    // 트랙 정지
    fun stopTrack() {
        _currentTrack.value = null
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        _isPlaying.value = false
        resetTimer() // 타이머 초기화
    }

    suspend fun previousTrack() {
        val currentIndex =
            _playerTrackList.value.indexOfFirst { it.trackId == _currentTrack.value?.trackId }
        if (currentIndex > 0) {
            playTrack(_playerTrackList.value[currentIndex - 1].trackId)
        } else {
//            stopTrack() // 첫 곡이면 정지
            playTrack(_playerTrackList.value.last().trackId) // 첫 곡이면 마지막 곡으로 돌아감
        }
    }

    suspend fun nextTrack() {
        val currentIndex =
            _playerTrackList.value.indexOfFirst { it.trackId == _currentTrack.value?.trackId }
        if (currentIndex != -1 && currentIndex < _playerTrackList.value.size - 1) {
            playTrack(_playerTrackList.value[currentIndex + 1].trackId)
        } else {
//            stopTrack() // 마지막 곡이면 정지
            playTrack(_playerTrackList.value[0].trackId) // 마지막 곡이면 첫 곡으로 돌아감
        }
    }

    suspend fun playPlaylist(tracks: List<TrackEssential>) {
        // 플레이리스트 재생
        _playerTrackList.value = tracks
        playTrack(tracks[0].trackId)
    }

    fun setPlayerViewState(state: PlayerViewState) {
        _playerViewState.value = state
    }

    // 트랙 좋아요
    suspend fun likeTrack(trackId: Int): Boolean {
        return try {
            val response = if (_currentTrack.value?.isLiked == true) {
                trackService.unlikeTrack(trackId.toString())
            } else {
                trackService.likeTrack(
                    TrackRequest.LikeTrackRequest(
                        trackId = trackId
                    )
                )
            }
            if (response.code == "SU") {
                // 트랙 상태 업데이트
                val updateTrack =
                    trackService.getTrackDetail(_currentTrack.value!!.trackId.toString())
                _currentTrack.value = updateTrack.payload
                true
            } else {
                Log.d("TrackPlayViewModel", "Failed to like track: ${response.message}")
                false
            }
        } catch (e: Exception) {
            Log.d("TrackPlayViewModel", "Error liking track: ${e.message}")
            false
        }
    }


    // 현재 재생 트랙의 댓글
    private val _commentList = MutableStateFlow<List<TrackResponse.GetTrackComment>?>(emptyList())
    val commentList: StateFlow<List<TrackResponse.GetTrackComment>?> get() = _commentList

    // 댓글 조회
    suspend fun getTrackComment(trackId: String) {
        try {
            val response = trackService.getTrackComments(trackId)
            if (response.code == "SU") {
                _commentList.value = response.payload
            } else {
                Log.d("TrackPlayViewModel", "Failed to get track comments: ${response.message}")
                _commentList.value = emptyList()
            }
        } catch (e: Exception) {
            Log.d("TrackPlayViewModel", "Error fetching track comments: ${e.message}")
            _commentList.value = emptyList()
        }
    }

    // 댓글 작성
    suspend fun createTrackComment(trackId: Int, comment: String): Boolean {
        val request = TrackRequest.CreateCommentRequest(
            trackId = trackId,
            context = comment
        )
        return try {
            val response = trackService.createTrackComment(request)
            if (response.code == "SU") {
                getTrackComment(_currentTrack.value?.trackId.toString()) // 댓글 작성 후 댓글 리스트 갱신
                true
            } else {
                Log.d("TrackPlayViewModel", "Failed to post track comment: ${response.message}")
                false
            }
        } catch (e: Exception) {
            Log.d("TrackPlayViewModel", "Error posting track comment: ${e.message}")
            false
        }
    }

    // 댓글 수정
    suspend fun updateTrackComment(commentId: Int, comment: String): Boolean {
        val request = TrackRequest.UpdateCommentRequest(
            commentId = commentId,
            context = comment
        )
        return try {
            val response = trackService.updateTrackComment(request)
            if (response.code == "SU") {
                getTrackComment(_currentTrack.value?.trackId.toString()) // 댓글 수정 후 댓글 리스트 갱신
                true
            } else {
                Log.d("TrackPlayViewModel", "Failed to update track comment: ${response.message}")
                false
            }
        } catch (e: Exception) {
            Log.d("TrackPlayViewModel", "Error updating track comment: ${e.message}")
            false
        }
    }

    // 댓글 삭제
    suspend fun deleteTrackComment(commentId: Int): Boolean {
        return try {
            val response = trackService.deleteTrackComment(commentId.toString())
            if (response.code == "SU") {
                getTrackComment(_currentTrack.value?.trackId.toString()) // 댓글 삭제 후 댓글 리스트 갱신
                true
            } else {
                Log.d("TrackPlayViewModel", "Failed to delete track comment: ${response.message}")
                false
            }
        } catch (e: Exception) {
            Log.d("TrackPlayViewModel", "Error deleting track comment: ${e.message}")
            false
        }
    }

    // 트랙 재생 타이머 (내부 로직)
    // 재생중인 상태에만 타이머가 작동
    // 재생 일시정지 -> 타이머 일시정지
    // 재생 정지 또는 트랙 변경 -> 재생 로그 기록, 타이머 초기화
    private val _playTime = MutableStateFlow(0L)
    private var _timerTask: Timer? = null
    private fun startTimer() {
        _timerTask = timer(period = 1000) {
            _playTime.value++
            // 15초 지나면 재생 기록
            if (_playTime.value == _LOG_TIME) {
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        val response = trackService.increasePlayCount(
                            TrackRequest.TrackPlayCountRequest(
                                trackId = _currentTrack.value?.trackId ?: 0
                            )
                        )
                        if (response.code == "SU") {
                            Log.d("TrackPlayViewModel", "재생 기록 성공")
                        } else {
                            Log.d("TrackPlayViewModel", "재생 기록 실패: ${response.message}")
                        }
                    } catch (e: Exception) {
                        Log.d("TrackPlayViewModel", "Error recording play count: ${e.message}")
                    }
                }
            }
        }
    }

    private fun pauseTimer() {
        _timerTask?.cancel()
    }

    private fun resetTimer() {
        _timerTask?.cancel()
        _playTime.value = 0L
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
    PLAYLIST
}
