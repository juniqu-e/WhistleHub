package com.whistlehub.workstation.data

import java.util.UUID
import kotlin.random.Random

data class Layer(
    val id: Int,
    val name: String,
    val description: String = "",
    val category: String = "",  // 예: "DRUM", "BASS", "OTHERS"
    val colorHex: String? = null
)

data class Track(
    val id: Int,
    val name: String,
    val layers: List<Layer> = emptyList()
)