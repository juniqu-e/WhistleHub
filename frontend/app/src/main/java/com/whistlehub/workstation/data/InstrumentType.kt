package com.whistlehub.workstation.data

import androidx.compose.ui.graphics.Color

enum class InstrumentType(
    val label: String,
    val assetFolder: String,
    val hexColor: Color,
    val typeNumber: Int
) {
    RECORD("RECORD", "whistle", Color(0xFFFF8585), 0),
    DRUM("DRUM", "drum", Color(0xFFFFEE58), 1),
    BASS("BASS", "bass", Color(0xFF9575CD), 2),
    GUITAR("GUITAR", "guitar", Color(0xFFFFAF4D), 3),
    SYNTH("SYNTH", "synth", Color(0xFFBDBDBD), 4),
    SEARCH("SEARCH", "", Color(0xFF80CBC4), 99),
}

enum class LayerButtonType(
    val label: String,
    val hexColor: Color,
    val typeNumber: Int
) {
    RECORD("RECORD", Color(0xFFFF8585), 44),
    SEARCH("SEARCH", Color(0xFF80CBC4), 99),
}