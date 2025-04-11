package com.whistlehub.workstation.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Recommend
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

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
    val typeNumber: Int,
    val iconStyle: ImageVector
) {
    RECORD("RECORD", Color(0xFFFF8585), 44, Icons.Filled.Mic),
    SEARCH("BROWSE", Color(0xFF80CBC4), 99, Icons.Filled.Search),
    RECOMMEND("RECOMMEND", Color(0xFF11A212), 1129, Icons.Filled.Recommend),
}