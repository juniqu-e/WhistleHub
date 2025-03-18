package com.whistlehub.playlist.data

import android.net.Uri
import java.util.Date

data class Artist(
    val id: String, val nickname: String, val profileImage: Uri
)

data class Track(
    val id: String,
    val title: String,
    val description: String,
    val artist: Artist,
    val isLike: Boolean,
    val duration: Int,
    val imageUrl: String,
    val importCount: Int,
    val likeCount: Int,
    val viewCount: Int,
    val createdAt: Date,
    val sourceTrack: List<Track>?,
    val importTrack: List<Track>?,
    val tags: List<String>?,
    val uri: Uri
)
