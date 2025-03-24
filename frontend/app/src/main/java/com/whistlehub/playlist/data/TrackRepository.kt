package com.whistlehub.playlist.data

import android.content.Context
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date
import javax.inject.Inject

interface TrackRepository {
    fun getTracks(): List<Track>
    fun addTrack(track: Track): Boolean
    fun removeTrack(trackId: String): Boolean
    fun updateTrack(track: Track): Boolean
    fun getTrackById(trackId: String): Track?
}

class TrackRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : TrackRepository {

    private val _trackList = MutableStateFlow<List<Track>>(listOf(
        Track(
            "1",
            "Song 1",
            "Artist 1",
            artist = Artist(
                id = "1",
                nickname = "Artist 1",
                profileImage = "https://picsum.photos/200/300?random=1".toUri()
            ),
            isLike = false,
            duration = 500,
            imageUrl = "https://picsum.photos/200/300?random=1",
            importCount = 2,
            likeCount = 2,
            viewCount = 2,
            createdAt = Date(),
            sourceTrack = null,
            importTrack = null,
            tags = null,
            uri = "https://whistlehub.s3.ap-northeast-2.amazonaws.com/%EA%B5%AD%EC%95%85+%ED%9A%A8%EA%B3%BC%EC%9D%8C+%23542.mp3".toUri()
        ),
        Track(
            "2",
            "Song 2",
            "Artist 2",
            artist = Artist(
                id = "1",
                nickname = "Artist 1",
                profileImage = "https://picsum.photos/200/300?random=1".toUri()
            ),
            isLike = true,
            duration = 500,
            imageUrl = "https://picsum.photos/200/300?random=2",
            importCount = 2,
            likeCount = 2,
            viewCount = 2,
            createdAt = Date(),
            sourceTrack = null,
            importTrack = null,
            tags = null,
            uri = "https://whistlehub.s3.ap-northeast-2.amazonaws.com/%EA%B5%AD%EC%95%85+%ED%9A%A8%EA%B3%BC%EC%9D%8C+%23542.mp3".toUri()
        ),
        Track(
            "3",
            "Song 3",
            "Artist 3",
            artist = Artist(
                id = "1",
                nickname = "Artist 1",
                profileImage = "https://picsum.photos/200/300?random=1".toUri()
            ),
            isLike = false,
            duration = 500,
            imageUrl = "https://picsum.photos/200/300?random=3",
            importCount = 2,
            likeCount = 2,
            viewCount = 2,
            createdAt = Date(),
            sourceTrack = null,
            importTrack = null,
            tags = listOf("tag1", "tag2"),
            uri = "https://whistlehub.s3.ap-northeast-2.amazonaws.com/%EA%B5%AD%EC%95%85+%ED%9A%A8%EA%B3%BC%EC%9D%8C+%23542.mp3".toUri()
        )
    ))
    val trackList: StateFlow<List<Track>> get() = _trackList

    override fun getTracks(): List<Track> {
        // 임시 트랙 리스트 (여기서는 예시로 하드코딩)
        return _trackList.value
    }

    override fun addTrack(track: Track): Boolean {
        // 트랙 추가 로직 (여기서는 예시로 단순히 리스트에 추가)
        _trackList.value = _trackList.value + track
        return true
    }

    override fun removeTrack(trackId: String): Boolean {
        // 트랙 삭제 로직 (여기서는 예시로 리스트에서 제거)
        _trackList.value = _trackList.value.filter { it.id != trackId }
        return true
    }

    override fun updateTrack(track: Track): Boolean {
        // 트랙 업데이트 로직 (여기서는 예시로 리스트에서 업데이트)
        _trackList.value = _trackList.value.map {
            if (it.id == track.id) track else it
        }
        return true
    }

    override fun getTrackById(trackId: String): Track? {
        // 트랙 ID로 트랙 검색 (여기서는 예시로 리스트에서 검색)
        return _trackList.value.find { it.id == trackId }
    }
}
