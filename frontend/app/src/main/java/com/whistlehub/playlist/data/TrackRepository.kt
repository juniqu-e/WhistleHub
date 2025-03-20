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
}

class TrackRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : TrackRepository {

    private val _trackList = MutableStateFlow<List<Track>>(emptyList())
    val trackList: StateFlow<List<Track>> get() = _trackList

    override fun getTracks(): List<Track> {
        // 임시 트랙 리스트 (여기서는 예시로 하드코딩)
        return listOf(
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
        )
    }
}
