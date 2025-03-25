package com.whistlehub.workstation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.whistlehub.workstation.data.BottomBarActions
import com.whistlehub.workstation.data.Layer
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
                category = "DRUM"
            ),
            Layer(
                id = 2,
                name = "OTHERS",
                description = "css 90 full song water d#m 01",
                category = "OTHERS"
            ),
            Layer(
                id = 3,
                name = "BASS",
                description = "gbc bass 85 gorilla f#fm",
                category = "BASS"
            ),
            Layer(id = 4, name = "BASS", description = "bpm100 a bass20", category = "BASS")
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
                category = "NEW"
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

    val bottomBarActions = BottomBarActions(
        onPlayedClicked = { playAllLayers(_tracks.value) },
        onTrackSavedClicked = {},
        onTrackUploadClicked = {},
        onTrackDownloadClicked = {},
        onExitClicked = {}
    )
}