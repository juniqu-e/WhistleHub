package com.whistlehub.workstation.di

import android.util.Log
import com.whistlehub.workstation.data.Layer
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(DelicateCoroutinesApi::class)
class AudioLayerPlayerImpl @Inject constructor() : AudioLayerPlayer {
    override fun playAllLayers(layers: List<Layer>) {
        // 각 레이어의 재생을 비동기로 처리
        layers.forEach { layer ->
            // 코루린 실행
            GlobalScope.launch {
                playLayer(layer)
            }
        }
    }

    private suspend fun playLayer(layer: Layer) {
        //오디오 엔진 호출
        Log.d("Play", "Play" + layer.name)
    }
}