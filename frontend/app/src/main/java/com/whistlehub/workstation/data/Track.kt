package com.whistlehub.workstation.data

data class Layer(
    val id: Int = 0,
    val name: String,
    val description: String = "",
    val category: String = "",  // 예: "DRUM", "BASS", "OTHERS"
    val instrumentType: Int,
    val colorHex: String? = null,
    val length: Int,
    val patternBlocks: List<PatternBlock> = emptyList(),
    val wavPath: String = "",
) {
    val beatPattern: List<Boolean>
        get() = MutableList(64) { false }.apply {
            patternBlocks.forEach { block ->
                for (i in block.start until (block.start + block.length).coerceAtMost(64)) {
                    this[i] = true
                }
            }
        }
}

data class PatternBlock(val start: Int, val length: Int)

data class Track(
    val id: Int,
    val name: String,
    val layers: List<Layer> = emptyList()
)

fun Layer.toAudioInfo(): LayerAudioInfo {
    return LayerAudioInfo(
        id = this.id,
        wavPath = this.wavPath,
        patternBlocks = this.patternBlocks
    )
}