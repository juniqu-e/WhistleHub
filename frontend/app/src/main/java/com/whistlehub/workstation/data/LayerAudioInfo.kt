package com.whistlehub.workstation.data

data class LayerAudioInfo(
    val id: Int,
    val patternBlocks: List<PatternBlock>,
    val samplePath: String,
    val lengthSeconds: Float,
)
