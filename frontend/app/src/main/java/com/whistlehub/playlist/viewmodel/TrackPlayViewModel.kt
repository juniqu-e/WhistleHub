package com.whistlehub.playlist.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whistlehub.WhistleHub
import com.whistlehub.common.data.local.entity.UserEntity
import com.whistlehub.common.data.local.room.UserRepository
import com.whistlehub.common.data.remote.dto.request.TrackRequest
import com.whistlehub.common.data.remote.dto.response.TrackResponse
import com.whistlehub.common.data.repository.TrackService
import com.whistlehub.playlist.data.TrackEssential
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.timer

@HiltViewModel
class TrackPlayViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val trackService: TrackService,
    val userRpository: UserRepository,
) : ViewModel() {
    // exoPlayer 관련 변수
    // 앱 context
    private val app = context.applicationContext as WhistleHub

    // ExoPlayer 인스턴스
    val exoPlayer get() = app.exoPlayer

    // 현재 재생 중인 트랙
    val currentTrack: StateFlow<TrackResponse.GetTrackDetailResponse?> get() = app.currentTrack

    // 재생 상태 (재생 중/일시 정지)
    val isPlaying: StateFlow<Boolean> get() = app.isPlaying

    // 현재 트랙 위치
    val playerPosition: StateFlow<Long> get() = app.playerPosition

    // 현재 트랙 길이
    val trackDuration: StateFlow<Long> get() = app.trackDuration

    // 플레이어 내부 트랙 리스트
    val playerTrackList: MutableStateFlow<List<TrackEssential>> get() = app.playerTrackList

    // 반복 재생 여부
    val isLooping: StateFlow<Boolean> get() = app.isLooping

    // 셔플 재생 여부
    val isShuffle: StateFlow<Boolean> get() = app.isShuffle


    // 내부 고유 상태
    // 현재 유저 정보
    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> get() = _user

    // 플레이어 화면 상태
    private val _playerViewState = MutableStateFlow<PlayerViewState>(PlayerViewState.PLAYING)
    val playerViewState: StateFlow<PlayerViewState> get() = _playerViewState

    // 트랙 로그 기준 시간
    private val _LOG_TIME = 15L

    init {
        // 빌드 할 때 트랙 리스트 추가, 유저정보 파싱
        viewModelScope.launch(Dispatchers.IO) {
            // 유저 정보 가져오기
            _user.value = userRpository.getUser()
        }
    }

    // 대시보드용 리스트 갱신
    // 팔로우 한 사람의 최신 트랙
    suspend fun getFollowRecentTracks(size: Int = 5): List<TrackEssential> {
        try {
            val response = trackService.getFollowingRecentTracks(size)
            if (response.code == "SU") {
                return response.payload?.map { track ->
                    TrackEssential(
                        trackId = track.trackId,
                        title = track.title,
                        artist = track.nickname,
                        imageUrl = track.imageUrl
                    )
                } ?: emptyList()
            } else {
                Log.d("TrackPlayViewModel", "Failed to get follow tracks: ${response.message}")
                return emptyList()
            }
        } catch (e: Exception) {
            Log.d("TrackPlayViewModel", "Error fetching follow tracks: ${e.message}")
            return emptyList()
        }
    }

    // 최근 들은 곡 조회
    suspend fun getRecentTrackList(size: Int = 3): List<TrackEssential> {
        try {
            val response = trackService.getRecentTracks(size)
            if (response.code == "SU") {
                return response.payload?.map { track ->
                    TrackEssential(
                        trackId = track.trackId,
                        title = track.title,
                        artist = track.nickname,
                        imageUrl = track.imageUrl
                    )
                } ?: emptyList()
            } else {
                Log.d("TrackPlayViewModel", "Failed to get recent tracks: ${response.message}")
                return emptyList()
            }
        } catch (e: Exception) {
            Log.d("TrackPlayViewModel", "Error fetching recent tracks: ${e.message}")
            return emptyList()
        }
    }

    // 특정 트랙과 비슷한 느낌의 트랙 리스트
    suspend fun getSimilarTrackList(trackId: Int): List<TrackEssential> {
        try {
            val response = trackService.getSimilarTracks(trackId)
            if (response.code == "SU") {
                return response.payload?.map { track ->
                    TrackEssential(
                        trackId = track.trackId,
                        title = track.title,
                        artist = track.nickname,
                        imageUrl = track.imageUrl
                    )
                } ?: emptyList()
            } else {
                Log.d("TrackPlayViewModel", "Failed to get similar tracks: ${response.message}")
                return emptyList()
            }
        } catch (e: Exception) {
            Log.d("TrackPlayViewModel", "Error fetching similar tracks: ${e.message}")
            return emptyList()
        }
    }

    // 한 번도 들어본 적 없는 음악
    suspend fun getNeverTrackList(size: Int = 3): List<TrackEssential> {
        try {
            val response = trackService.getNeverTracks(size)
            if (response.code == "SU") {
                return response.payload?.map { track ->
                    TrackEssential(
                        trackId = track.trackId,
                        title = track.title,
                        artist = track.nickname,
                        imageUrl = track.imageUrl
                    )
                } ?: emptyList()
            } else {
                Log.d("TrackPlayViewModel", "Failed to get never tracks: ${response.message}")
                return emptyList()
            }
        } catch (e: Exception) {
            Log.d("TrackPlayViewModel", "Error fetching never tracks: ${e.message}")
            return emptyList()
        }
    }

    // 팔로우한 사람 한 명
    suspend fun getFollowingMember(): TrackResponse.MemberInfo {
        try {
            val response = trackService.getFollowingMember()
            if (response.code == "SU") {
                return response.payload ?: TrackResponse.MemberInfo(
                    memberId = 0,
                    nickname = "",
                    profileImage = ""
                )

            } else {
                Log.d("TrackPlayViewModel", "Failed to get following members: ${response.message}")
                return TrackResponse.MemberInfo(
                    memberId = 0,
                    nickname = "",
                    profileImage = ""
                )
            }
        } catch (e: Exception) {
            Log.d("TrackPlayViewModel", "Error fetching following members: ${e.message}")
            return TrackResponse.MemberInfo(
                memberId = 0,
                nickname = "",
                profileImage = ""
            )
        }
    }

    // 특정 회원의 Fanmix 트랙 리스트
    suspend fun getFanMixTracks(memberId: Int, size: Int = 10): List<TrackEssential> {
        try {
            val response = trackService.getFanMixTracks(memberId, size)
            if (response.code == "SU") {
                return response.payload?.map { track ->
                    TrackEssential(
                        trackId = track.trackId,
                        title = track.title,
                        artist = track.nickname,
                        imageUrl = track.imageUrl
                    )
                } ?: emptyList()
            } else {
                Log.d("TrackPlayViewModel", "Failed to get fan mix tracks: ${response.message}")
                return emptyList()
            }
        } catch (e: Exception) {
            Log.d("TrackPlayViewModel", "Error fetching fan mix tracks: ${e.message}")
            return emptyList()
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
            resetTimer()
            if (app.playTrack(trackId)) {
                getTrackComment(trackId.toString())  // 댓글 정보 업데이트
                startTimer() // 타이머 시작
                return
            }
        } catch (e: Exception) {
            Log.e("TrackPlayViewModel", "Error playing track: ${e.message}")
        }
    }

    // 트랙 일시 정지
    fun pauseTrack() {
        app.pauseTrack()
        pauseTimer() // 타이머 일시 정지
    }

    // 트랙 재개
    fun resumeTrack() {
        app.resumeTrack()
        startTimer() // 타이머 재개
    }

    // 트랙 정지
    fun stopTrack() {
        app.stopTrack()
        resetTimer() // 타이머 초기화
    }

    suspend fun previousTrack() {
        app.previousTrack()
    }

    suspend fun nextTrack() {
        app.nextTrack()
    }

    fun toggleLooping() {
        app.toggleLooping()
    }

    fun toggleShuffle() {
        app.toggleShuffle()
    }

    // 재생목록 비우기 (로그아웃 시 호출)
    fun clearTrackList() {
        app.clearTrackList()
    }

    // 트랙 신고
    suspend fun reportTrack(trackId: Int, type: Int = 1, detail: String?): Boolean {
        return try {
            val response = trackService.reportTrack(
                TrackRequest.ReportTrackRequest(
                    trackId = trackId,
                    type = type,  // 1: 저작권, 2: 불량 트랙,
                    detail = detail
                )
            )
            if (response.code == "SU") {
                true
            } else {
                Log.d("TrackPlayViewModel", "Failed to report track: ${response.message}")
                false
            }
        } catch (e: Exception) {
            Log.d("TrackPlayViewModel", "Error reporting track: ${e.message}")
            false
        }
    }

    suspend fun playPlaylist(tracks: List<TrackEssential>) {
        // 플레이리스트 재생
        app.setTrackList(tracks)
        playTrack(tracks[0].trackId)
    }

    fun setPlayerViewState(state: PlayerViewState) {
        _playerViewState.value = state
    }

    // 트랙 좋아요
    suspend fun likeTrack(trackId: Int): Boolean {
        return try {
            val response = if (currentTrack.value?.isLiked == true) {
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
                    trackService.getTrackDetail(currentTrack.value!!.trackId.toString())
                if (updateTrack.payload != null) {
                    app.setCurrentTrack(updateTrack.payload)
                }
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
                getTrackComment(currentTrack.value?.trackId.toString()) // 댓글 작성 후 댓글 리스트 갱신
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
                getTrackComment(currentTrack.value?.trackId.toString()) // 댓글 수정 후 댓글 리스트 갱신
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
                getTrackComment(currentTrack.value?.trackId.toString()) // 댓글 삭제 후 댓글 리스트 갱신
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
                                trackId = currentTrack.value?.trackId ?: 0
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
}

enum class PlayerViewState {
    PLAYING,
    COMMENT,
    PLAYLIST
}
