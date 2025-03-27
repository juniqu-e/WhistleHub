package com.whistlehub.common.util

object AudioEngineBridge {
    init {
        System.loadLibrary("whistlehub")
    }

    external fun startAudioEngine(): Int
    external fun stopAudioEngine(): Int
}