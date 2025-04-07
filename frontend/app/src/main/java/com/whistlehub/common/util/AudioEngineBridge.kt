package com.whistlehub.common.util

import com.whistlehub.workstation.data.LayerAudioInfo

interface PlaybackListener {
    fun onPlaybackFinished()
}

object AudioEngineBridge {
    init {
        System.loadLibrary("whistlehub")
    }

    external fun startAudioEngine(): Int
    external fun stopAudioEngine(): Int

    external fun setLayers(layers: List<LayerAudioInfo>, maxUsedBars: Int)

    external fun renderMixToWav(outputPath: String): Boolean

    external fun setCallback(listener: PlaybackListener)
}