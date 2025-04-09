package com.whistlehub.workstation.data

data class LayerAudioInfo(
    val id: Int,
    val wavPath: String,
    val patternBlocks: List<PatternBlock>,
    val volume: Float,
    val playbackRate: Float,
)
