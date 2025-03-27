package com.whistlehub.workstation.di

import com.whistlehub.workstation.data.Layer

interface AudioLayerPlayer {
    fun playAllLayers(layers: List<Layer>)
}