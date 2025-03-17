package com.whistlehub.playlist.data

import android.net.Uri

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val uri: Uri
)
