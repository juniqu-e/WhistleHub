package com.whistlehub.workstation.data

data class UploadMetadata(
    val title: String,
    val description: String,
    val visibility: Int,
    val tags: List<Int>,
)
