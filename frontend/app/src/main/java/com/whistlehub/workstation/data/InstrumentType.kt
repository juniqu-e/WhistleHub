package com.whistlehub.workstation.data

import androidx.compose.ui.graphics.Color

enum class InstrumentType(val label: String, val assetFolder: String, val hexColor: Color) {
    DRUM("DRUM", "drum", Color(0xFFFFEE58)),
    BASS("BASS", "bass", Color(0xFF9575CD)),
    GUITAR("GUITAR", "guitar", Color(0xFFFFAF4D)),
    SYNTH("SYNTH", "synth", Color(0xFFBDBDBD)),
    SEARCH("SEARCH", "", Color(0xFF80CBC4))
}
