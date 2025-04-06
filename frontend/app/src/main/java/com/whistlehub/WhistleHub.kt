package com.whistlehub

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.whistlehub.common.data.remote.dto.response.TrackResponse
import com.whistlehub.common.data.repository.TrackService
import com.whistlehub.playlist.data.TrackEssential
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

@HiltAndroidApp
class WhistleHub : Application() {
    // ExoPlayer 인스턴스
    lateinit var exoPlayer: ExoPlayer

    // 플레이어 관련 상태를 저장할 StateFlow들
    val isPlaying = MutableStateFlow(false)
    val currentTrack = MutableStateFlow<TrackResponse.GetTrackDetailResponse?>(null)
    val playerPosition = MutableStateFlow(0L)
    val trackDuration = MutableStateFlow(0L)
    val playerTrackList = MutableStateFlow<List<TrackEssential>>(emptyList())
    val isLooping = MutableStateFlow(false)
    val isShuffle = MutableStateFlow(false)

    // 트랙 재생을 위한 trackService 주입
    @Inject
    lateinit var trackService: TrackService

    // 앱 라이프사이클 상태 추적
    private var appInBackground = false

    // 라이프사이클 옵저버
    private val lifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            if (appInBackground) {
                // 앱이 다시 포그라운드로 올 때 재생 상태 복원 (선택적)
                Log.d("Application", "App came to foreground")
            }
            appInBackground = false
        }

        override fun onStop(owner: LifecycleOwner) {
            appInBackground = true
            // 앱이 백그라운드로 갈 때 필요한 조치
            pauseTrack()
            Log.d("Application", "App went to background")
        }
    }

    override fun onCreate() {
        super.onCreate()

        // 앱 라이프사이클 옵저버 등록
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)

        // exoPlayer 초기화
        exoPlayer = ExoPlayer.Builder(this).build()

        // ExoPlayer 이벤트 리스너 설정
        setupExoPlayerListeners()

        // 플레이어 포지션 업데이트 코루틴 시작
        CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                if (exoPlayer.isPlaying) {
                    playerPosition.value = exoPlayer.currentPosition
                    trackDuration.value =
                        if (exoPlayer.duration != C.TIME_UNSET) exoPlayer.duration else 0L
                }
                delay(100)
            }
        }
    }

    override fun onTerminate() {
        // 앱이 완전히 종료될 때만 ExoPlayer 해제
        if (::exoPlayer.isInitialized) {
            exoPlayer.release()
        }
        super.onTerminate()
    }

    private fun setupExoPlayerListeners() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    playerPosition.value = 0L
                    trackDuration.value = 0L
                }
                when (state) {
                    Player.STATE_IDLE -> Log.d("ExoPlayer", "IDLE 상태")
                    Player.STATE_BUFFERING -> Log.d("ExoPlayer", "버퍼링 중")
                    Player.STATE_READY -> Log.d("ExoPlayer", "준비 완료, 재생 가능")
                    Player.STATE_ENDED -> {
                        Log.d("ExoPlayer", "재생 완료")
                        // 다음 곡 자동 재생 처리
                        CoroutineScope(Dispatchers.Main).launch {
                            nextTrack()
                        }
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("ExoPlayer", "재생 오류 발생: ${error.message}")
            }
        })
    }

    // ExoPlayer를 위한 다양한 메서드들
    suspend fun playTrack(trackId: Int): Boolean {
        try {
            val trackResponse = trackService.getTrackDetail(trackId.toString())  // 트랙 정보 가져오기
            val trackData = trackService.playTrack(trackId.toString())

            if (trackResponse.payload == null || trackData == null) {
                Log.d("TrackPlayViewModel", "Failed to get track detail: ${trackResponse.message}")
                stopTrack()
                false  // 트랙 정보 가져오기 실패 시 플레이어 종료 후 false 반환
            }
            val track = trackResponse.payload
            if (trackData != null) {
                val mediaItem = MediaItem.fromUri(byteArrayToUri(this, trackData) ?: Uri.EMPTY)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
                exoPlayer.play()

                currentTrack.value = track
                isPlaying.value = true

                // 플레이어 트랙 리스트 업데이트
                val existingIndex = playerTrackList.value.indexOfFirst { it.trackId == trackId }
                if (existingIndex == -1) {
                    playerTrackList.value += TrackEssential(
                        trackId = track!!.trackId,
                        title = track.title,
                        artist = track.artist.nickname,
                        imageUrl = track.imageUrl
                    )  // 현재 재생 목록 맨 뒤에 트랙 추가
                }
            }
            true  // 트랙 재생 성공 시 true 반환
        } catch (e: Exception) {
            Log.e("TrackPlayViewModel", "Error playing track: ${e.message}")
            false
        }
        return false
    }

    fun pauseTrack() {
        exoPlayer.playWhenReady = false
        exoPlayer.pause()
        isPlaying.value = false
    }

    fun resumeTrack() {
        exoPlayer.playWhenReady = true
        exoPlayer.play()
        isPlaying.value = true
    }

    fun stopTrack() {
        currentTrack.value = null
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        isPlaying.value = false
    }

    suspend fun previousTrack() {
        val currentIndex =
            playerTrackList.value.indexOfFirst { it.trackId == currentTrack.value?.trackId }
        if (currentIndex > 0) {
            playTrack(playerTrackList.value[currentIndex - 1].trackId)
        } else {
//            stopTrack() // 첫 곡이면 정지
            playTrack(playerTrackList.value.last().trackId) // 첫 곡이면 마지막 곡으로 돌아감
        }
    }

    suspend fun nextTrack() {
        val currentIndex =
            playerTrackList.value.indexOfFirst { it.trackId == currentTrack.value?.trackId }
        if (isShuffle.value) {
            // 셔플 모드면 랜덤 트랙으로 이동
            val randomIndex = (playerTrackList.value.indices).random()
            val randomTrackId = playerTrackList.value[randomIndex].trackId

            // 현재 트랙이랑 중복되면 한 번 더 뽑기 (선택사항)
            if (playerTrackList.value.size > 1 && playerTrackList.value[randomIndex].trackId == currentTrack.value?.trackId) {
                val otherIndex = (playerTrackList.value.indices - currentIndex).random()
                playTrack(playerTrackList.value[otherIndex].trackId)
            } else {
                playTrack(randomTrackId)
            }
        } else {
            // 일반 재생 로직
            if (currentIndex != -1 && currentIndex < playerTrackList.value.size - 1) {
                playTrack(playerTrackList.value[currentIndex + 1].trackId)
            } else {
                playTrack(playerTrackList.value[0].trackId)
            }
        }
    }

    fun toggleLooping() {
        isLooping.value = !isLooping.value
        exoPlayer.repeatMode =
            if (isLooping.value) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
    }

    fun toggleShuffle() {
        isShuffle.value = !isShuffle.value
        exoPlayer.shuffleModeEnabled = isShuffle.value
    }

    fun setTrackList(tracks: List<TrackEssential>) {
        playerTrackList.value = tracks
    }

    fun setCurrentTrack(track: TrackResponse.GetTrackDetailResponse) {
        currentTrack.value = track
    }

    fun clearTrackList() {
        // 로그아웃 시 호출 필요
        playerTrackList.value = emptyList()
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
}