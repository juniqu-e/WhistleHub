package com.whistlehub.common.util

import com.whistlehub.workstation.data.LayerAudioInfo

interface PlaybackListener {
    fun updateProgress(mProgress: Float)  // 진행률 업데이트
    fun onPlaybackFinished() // 재생 완료 콜백
    fun updateWaveformPoints(waveformPoints: List<Float>)  // 파형 데이터 업데이트
}

object AudioEngineBridge {
    init {
        System.loadLibrary("whistlehub")
    }

    external fun startAudioEngine(): Int
    external fun stopAudioEngine(): Int

    external fun setLayers(layers: List<LayerAudioInfo>, maxUsedBars: Int)

    external fun renderMixToWav(outputPath: String, totalFrames: Int): Boolean

    external fun generateWaveformPoints(wavFilePath: String)

    external fun getWavDurationSeconds(outputPath: String): Float

    external fun setCallback(listener: PlaybackListener)
}