package com.whistlehub.common.data.remote.dto.response

sealed class WorkstationResponse {
    data class WorkstationTrack(
        val trackId: String,
        val title: String,
        val description: String,
        val isPublic: Boolean,
        val genre: String,
        val tags: List<String>,
        val layers: List<Layer>
    )

    data class Layer(
        val layerId: String,
        val name: String,
        val type: String,
        val audioUrl: String,
        val isBase: Boolean
    )

    data class PublishResult(
        val trackId: String,
        val publishedAt: String
    )
}