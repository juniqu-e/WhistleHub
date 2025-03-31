package com.whistlehub.workstation.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.whistlehub.common.util.AudioEngineBridge.setLayers
import com.whistlehub.common.util.AudioEngineBridge.startAudioEngine
import com.whistlehub.workstation.data.BottomBarActions
import com.whistlehub.workstation.data.InstrumentType
import com.whistlehub.workstation.data.Layer
import com.whistlehub.workstation.data.LayerAudioInfo
import com.whistlehub.workstation.data.PatternBlock
import com.whistlehub.workstation.data.toAudioInfo
import com.whistlehub.workstation.di.AudioLayerPlayer
import com.whistlehub.workstation.di.WorkStationBottomBarProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject

@HiltViewModel
class WorkStationViewModel @Inject constructor(
    val bottomBarProvider: WorkStationBottomBarProvider,
    private val audioLayerPlayer: AudioLayerPlayer
) : ViewModel() {
    private val _tracks = MutableStateFlow<List<Layer>>(emptyList())
    val tracks: StateFlow<List<Layer>> = _tracks.asStateFlow()
    private val _nextId = mutableIntStateOf(1)
    private val _wavPathMap = mutableStateOf<Map<Int, String>>(emptyMap())
    val wavPathMap: State<Map<Int, String>> get() = _wavPathMap

    fun addLayerByInstrument(type: InstrumentType) {
        val newId = (_tracks.value.maxOfOrNull { it.id } ?: 0) + 1
        val newLayer = Layer(
            id = newId,
            name = type.label,
            description = "New ${type.label} Layer",
            category = type.name,
            length = 4,
            patternBlocks = emptyList(),
        )
        _tracks.value += listOf(newLayer)
    }

    fun addLayer(newLayer: Layer) {
        val newId = _nextId.value  // 현재 ID 가져오기
        _nextId.value += 1  // ID 증가
        // ID가 증가된 새로운 Layer 객체 생성
        val layerWithId = newLayer.copy(id = newId)
        // 레이어를 _tracks에 추가
        _tracks.value += layerWithId
    }

    fun deleteLayer(layer: Layer) {
        _tracks.value = _tracks.value.filter { it.id != layer.id }
        Log.d("Play", _tracks.value.toString())
    }

    private fun isOverlapping(newBlock: PatternBlock, existing: PatternBlock): Boolean {
        val rangeA = newBlock.start until (newBlock.start + newBlock.length)
        val rangeB = existing.start until (existing.start + existing.length)
        return rangeA.any { it in rangeB }
    }

    fun toggleBeat(layerId: Int, index: Int) {
        _tracks.value = _tracks.value.map { layer ->
            if (layer.id == layerId) {
                val blocks = layer.patternBlocks.toMutableList()
                val length = layer.length
                val newBlock = PatternBlock(index, length)
                val existing = blocks.find { index in it.start until (it.start + it.length) }

                if (existing != null) {
                    blocks.remove(existing)
                } else {
                    val overlaps = blocks.any { isOverlapping(newBlock, it) }
                    if (!overlaps) {
                        blocks.add(newBlock)
                    }
                }
                Log.d("Layer", layer.patternBlocks.toString())
                layer.copy(patternBlocks = blocks)

            } else layer
        }
    }

    fun applyPatternAutoRepeat(layerId: Int, startBeat: Int, interval: Int) {
        _tracks.value = _tracks.value.map { layer ->
            if (layer.id == layerId) {
                val blocks = layer.patternBlocks.toMutableList()
                val length = layer.length
                var i = startBeat
                while (i < 60) {
                    val newBlock = PatternBlock(i, length)
                    val alreadyExists = blocks.any { it.start == newBlock.start }
                    val overlaps = blocks.any { isOverlapping(newBlock, it) }
                    if (!overlaps) {
                        blocks.add(newBlock)
                    }
                    i += interval
                }

                layer.copy(patternBlocks = blocks)
            } else layer
        }
    }

    fun onPlayClicked() {
        val infos = getAudioLayerInfos();
        startAudioEngine();
        setLayers(infos);
        startAudioEngine();
    }

    private fun getAudioLayerInfos(): List<LayerAudioInfo> {
        return tracks.value.map { it.toAudioInfo() }
    }

    private fun audioEngineTest() {
        startAudioEngine()
    }

    val bottomBarActions = BottomBarActions(
        onPlayedClicked = {
//            playAllLayers(_tracks.value)
//            audioEngineTest()
        },
        onTrackSavedClicked = {},
        onTrackUploadClicked = {},
        onTrackDownloadClicked = {},
        onExitClicked = {}
    )
    //    fun toggleBeat(layerId: Int, index: Int) {
//        _tracks.value = _tracks.value.map { layer ->
//            if (layer.id == layerId) {
//                Log.d("Toggle", "Layer ${layer.id} toggle at $index")
//                val pattern = layer.beatPattern.toMutableList()
//                val length = layer.length
//                val isActive = pattern[index]
//
//                if (!isActive) {
//                    //중복 점유 체크
//                    val overlap = (index until (index + length)).any {
//                        it >= 60 || pattern[it]
//                    }
//                    if (overlap) {
//                        Log.d("Toggle", "Overlap - blocked")
//                        return@map layer
//                    }
//
//                    for (i in index until (index + length).coerceAtMost(60)) {
//                        pattern[i] = true
//                    }
//                } else {
//                    // 시작 지점 누르면 전체 해제
//                    for (i in index until (index + length).coerceAtMost(60)) {
//                        pattern[i] = false
//                    }
//                }
//
//                layer.copy(beatPattern = pattern)
//            } else layer
//        }
//    }
//    fun applyPatternAutoRepeat(layerId: Int, startBeat: Int, interval: Int) {
//        _tracks.value = _tracks.value.map { layer ->
//            if (layer.id == layerId) {
//                val pattern = layer.beatPattern.toMutableList()
//                val length = layer.length
//                var i = startBeat
//                while (i < 60) {
//                    for (j in 0 until length) {
//                        if (i + j < 60) {
//                            pattern[i + j] = true
//                        }
//                    }
//                    i += interval
//                }
//
//                layer.copy(beatPattern = pattern)
//            } else layer
//        }
//    }
}