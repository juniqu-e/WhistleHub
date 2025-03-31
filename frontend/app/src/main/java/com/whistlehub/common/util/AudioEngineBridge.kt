package com.whistlehub.common.util

import com.whistlehub.workstation.data.LayerAudioInfo

object AudioEngineBridge {
    init {
        System.loadLibrary("whistlehub")
    }

    external fun startAudioEngine(): Int
    external fun stopAudioEngine(): Int
    external fun sendLayerInfoToNative(layerInfo: LayerAudioInfo)

    external fun setLayers(layers : List<LayerAudioInfo>)
}