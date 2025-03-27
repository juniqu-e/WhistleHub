package com.whistlehub.workstation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.whistlehub.workstation.data.BottomBarActions
import com.whistlehub.workstation.data.Layer
import com.whistlehub.workstation.data.PatternBlock
import com.whistlehub.workstation.di.AudioLayerPlayer
import com.whistlehub.workstation.di.WorkStationBottomBarProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class WorkStationViewModel @Inject constructor(
    val bottomBarProvider: WorkStationBottomBarProvider,
    private val audioLayerPlayer: AudioLayerPlayer
) : ViewModel() {
    private val _tracks = MutableStateFlow(
        listOf(
            Layer(
                id = 1,
                name = "DRUM",
                description = "ba 95 drum loop fever full",
                category = "DRUM",
                length = 1,
            ),
            Layer(
                id = 2,
                name = "OTHERS",
                description = "css 90 full song water d#m 01",
                category = "OTHERS",
                length = 2,
            ),
            Layer(
                id = 3,
                name = "BASS",
                description = "gbc bass 85 gorilla f#fm",
                category = "BASS",
                length = 3,
            ),
            Layer(
                id = 4,
                name = "BASS",
                description = "bpm100 a bass20",
                category = "BASS",
                length = 4
            )
        )
    )
    val tracks: StateFlow<List<Layer>> = _tracks.asStateFlow()

    fun addLayer() {
        val newId = (_tracks.value.maxOfOrNull { it.id } ?: 0) + 1
        _tracks.value += listOf(
            Layer(
                id = newId,
                name = "Layer $newId",
                description = "New Layer",
                category = "NEW",
                length = 4
            )
        )
        Log.d("Play", _tracks.value.toString())
    }

    fun deleteLayer(layer: Layer) {
        _tracks.value = _tracks.value.filter { it.id != layer.id }
        Log.d("Play", _tracks.value.toString())
    }

    private fun playAllLayers(layers: List<Layer>) {
        audioLayerPlayer.playAllLayers(layers)
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

    fun generateBeatPatternFromBlocks(blocks: List<PatternBlock>): List<Boolean> {
        val pattern = MutableList(60) { false }
        blocks.forEach { block ->
            for (i in block.start until (block.start + block.length).coerceAtMost(60)) {
                pattern[i] = true
            }
        }
        return pattern
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
    val bottomBarActions = BottomBarActions(
        onPlayedClicked = { playAllLayers(_tracks.value) },
        onTrackSavedClicked = {},
        onTrackUploadClicked = {},
        onTrackDownloadClicked = {},
        onExitClicked = {}
    )
}