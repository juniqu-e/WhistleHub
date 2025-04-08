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
    val bpm : Float? = null,
    val volume : Float = 1.0f,
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

fun Layer.toAudioInfo(projectBpm: Float): LayerAudioInfo {
    val originalBpm = this.bpm ?: projectBpm
    val playbackRate = projectBpm / originalBpm

    return LayerAudioInfo(
        id = this.id,
        wavPath = this.wavPath,
        patternBlocks = this.patternBlocks,
        volume = this.volume,
        playbackRate = playbackRate
    )
}


// Define a function to map instrumentType to category and colorHex
fun getCategoryAndColorHex(instrumentType: Int): Pair<String, String> {
    return when (instrumentType) {
        0 -> Pair("Record", "#7A7A7A") // Record (Tone down further)
        1 -> Pair("Whistle", "#B7A700") // Whistle (Yellow, tone down more)
        2 -> Pair("Acoustic Guitar", "#5F7320") // Acoustic Guitar (Green, tone down more)
        3 -> Pair("Voice", "#D14400") // Voice (Orange, tone down more)
        4 -> Pair("Drums", "#E67E00") // Drums (Amber, tone down more)
        5 -> Pair("Bass", "#5D1070") // Bass (Purple, tone down more)
        6 -> Pair("Electric Guitar", "#144D9A") // Electric Guitar (Blue, tone down more)
        7 -> Pair("Piano", "#2A2D72") // Piano (Indigo, tone down more)
        8 -> Pair("Synth", "#2C6D2B") // Synth (Green, tone down more)
        else -> Pair("Unknown", "#7A7A7A") // Default case (Tone down gray)
    }
}

fun roundUpToNearestPowerOfTwo(value: Float): Int {
    // 1, 2, 4, 8 등으로 올림
    return when {
        value <= 1 -> 1
        value <= 2 -> 2
        value <= 4 -> 4
        else -> 8
    }
}