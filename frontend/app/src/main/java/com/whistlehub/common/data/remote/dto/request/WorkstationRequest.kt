package com.whistlehub.common.data.remote.dto.request

sealed class WorkstationRequest {
    data class CreateTrack(
        val title: String,
        val description: String,
        val isPublic: Boolean,
        val genre: String,
        val tags: List<String>
    )

    data class SaveTrack(
        val trackId: String,
        val layers: List<SaveLayer>
    )

    data class SaveLayer(
        val layerId: String?,
        val name: String,
        val type: String,
        val audioData: ByteArray,
        val isBase: Boolean
    )

    data class PublishTrack(
        val trackId: String
    )
}