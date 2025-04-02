package com.whistlehub.workstation.data

import androidx.compose.ui.graphics.Color

enum class InstrumentType(
    val label: String,
    val assetFolder: String,
    val hexColor: Color,
    val typeNumber: Int
) {
    DRUM("DRUM", "drum", Color(0xFFFFEE58), 0),
    BASS("BASS", "bass", Color(0xFF9575CD), 1),
    GUITAR("GUITAR", "guitar", Color(0xFFFFAF4D), 2),
    SYNTH("SYNTH", "synth", Color(0xFFBDBDBD), 3),
    RECORD("RECORD", "record", Color(0xFFFF8585), 4),
    SEARCH("SEARCH", "", Color(0xFF80CBC4), 5),
}
