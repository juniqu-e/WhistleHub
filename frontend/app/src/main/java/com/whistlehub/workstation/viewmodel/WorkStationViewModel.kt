package com.whistlehub.workstation.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whistlehub.common.data.remote.dto.request.TrackRequest
import com.whistlehub.common.data.remote.dto.request.WorkstationRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.TrackResponse
import com.whistlehub.common.data.remote.dto.response.WorkstationResponse
import com.whistlehub.common.data.repository.TrackService
import com.whistlehub.common.data.repository.WorkstationService
import com.whistlehub.common.util.AudioEngineBridge.renderMixToWav
import com.whistlehub.common.util.AudioEngineBridge.setCallback
import com.whistlehub.common.util.AudioEngineBridge.setLayers
import com.whistlehub.common.util.AudioEngineBridge.startAudioEngine
import com.whistlehub.common.util.AudioEngineBridge.stopAudioEngine
import com.whistlehub.common.util.PlaybackListener
import com.whistlehub.common.util.createMultipart
import com.whistlehub.common.util.createRequestBody
import com.whistlehub.common.util.downloadWavFromS3Url
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
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class WorkStationViewModel @Inject constructor(
    val bottomBarProvider: WorkStationBottomBarProvider,
    private val audioLayerPlayer: AudioLayerPlayer,
    private val trackService: TrackService,
    private val workstationService: WorkstationService,
) : ViewModel(), PlaybackListener {
    private val _tracks = MutableStateFlow<List<Layer>>(emptyList())
    val tracks: StateFlow<List<Layer>> = _tracks.asStateFlow()
    private val _nextId = mutableIntStateOf(1)
    private val _wavPathMap = mutableStateOf<Map<Int, String>>(emptyMap())
    val wavPathMap: State<Map<Int, String>> get() = _wavPathMap
    private val _searchTrackResults =
        MutableStateFlow<ApiResponse<List<TrackResponse.SearchTrack>>?>(null);
    val searchTrackResults: StateFlow<ApiResponse<List<TrackResponse.SearchTrack>>?> get() = _searchTrackResults;
    private val _layersOfSearchTrack =
        mutableStateOf<ApiResponse<WorkstationResponse.ImportTrackResponse>?>(null);
    val layersOfSearchTrack: State<ApiResponse<WorkstationResponse.ImportTrackResponse>?> get() = _layersOfSearchTrack;
    private val _isPlaying = mutableStateOf(false)
    val isPlaying: State<Boolean> get() = _isPlaying

    init {
        setCallback(this)
    }

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
        val newId = _nextId.intValue  // 현재 ID 가져오기
        _nextId.intValue += 1  // ID 증가
        // ID가 증가된 새로운 Layer 객체 생성
        val layerWithId = newLayer.copy(id = newId)
        // 레이어를 _tracks에 추가
        _tracks.value += layerWithId

        Log.d("Search", "In LOCAL : ${_tracks.value.size}")
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
//                    val alreadyExists = blocks.any { it.start == newBlock.start }
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

    fun searchTrack(request: TrackRequest.SearchTrackRequest) {
        viewModelScope.launch {
            try {
                val results = trackService.searchTracks(request);
                _searchTrackResults.value = results;
                Log.d("Search", results.toString());
            } catch (e: Exception) {
                Log.d("Search", "검색 오류 ${e.message}");
            }
        }
    }

    fun addLayerFromSearchTrack(request: WorkstationRequest.ImportTrackRequest, context: Context) {
        viewModelScope.launch {
            try {
                val results = workstationService.importTrack(request);
//                _layersOfSearchTrack.value = results;
                val track = results.payload ?: return@launch
                val layers = track.layers.map { layerRes ->
                    val s3Url = layerRes.soundUrl

                    Log.d("Search", "S3 Url : $s3Url")
                    Log.d("Search", "layer : $layerRes")
                    val fileName = "layer_${UUID.randomUUID()}.wav"
                    val localFile = downloadWavFromS3Url(context, s3Url, fileName)
                    Log.d("Search", "불러온 레이어 수: $localFile")
                    Layer(
                        name = layerRes.name,
                        description = track.title,
                        category = layerRes.instrumentType.toString(),
                        colorHex = "#BDBDBD",
                        length = 4,
                        wavPath = localFile.absolutePath
                    )
                }

                layers.forEach { layer ->
                    addLayer(layer)
                }
//                _tracks.value += layers
                Log.d("Search", "불러온 레이어 수: ${layers.size}")
            } catch (e: Exception) {
                Log.d("Search", "Layer 오류 ${e.message}");
            }
        }
    }

    fun onPlayClicked() {
        val infos = getAudioLayerInfos();
        val maxUsedBars = getMaxUsedBars(tracks.value)
        if (_isPlaying.value) {
            stopAudioEngine()
        } else {
            stopAudioEngine()
            setLayers(infos, maxUsedBars)
            startAudioEngine()
        }
        _isPlaying.value = !_isPlaying.value
    }

    fun onUpload(context: Context, fileName: String, onResult: (Boolean) -> Unit = {}) {
        val safeFileName = if (fileName.endsWith(".wav")) fileName else "$fileName.wav"
        val mix = File(context.filesDir, safeFileName)

        viewModelScope.launch {
            val infos = getAudioLayerInfos();
//            setLayers(infos, maxUsedBars)
            val success = renderMixToWav(mix.absolutePath)

            if (!success) {
                onResult(false)
            } else {
                //MultiPart
                val requestBodyMap = hashMapOf(
                    "title" to createRequestBody(fileName), //
                    "description" to createRequestBody("Description Hard Coding (。・ω・。)"),
                    "duration" to createRequestBody("120"),
                    "visibility" to createRequestBody("1"), //
                    "tags" to createRequestBody("1,2,3,4"), //
                    "sourceTracks" to createRequestBody("1,2"),
                    "layerName" to createRequestBody("layer1, layer2"),
                    "instrumentType" to createRequestBody("1,2")
                )
                val trackImg = null
                val trackSoundFile = createMultipart(mix, "trackSoundFile")
                val layerSoundFiles = tracks.value.map { layer ->
                    Log.d("Upload", File(layer.wavPath).toString())
                    createMultipart(File(layer.wavPath), "layerSoundFiles")
                }

                requestBodyMap.forEach { (key, value) ->
                    Log.d("Upload", "$key => ${value.contentType()}")
                }

                Log.d(
                    "Upload",
                    "TrackSoundFile -> name: ${trackSoundFile.headers}, body type: ${trackSoundFile.body.contentType()}"
                )

                layerSoundFiles.forEachIndexed { index, part ->
                    Log.d(
                        "Upload",
                        "Layer[$index] -> name: ${part.headers}, body type: ${part.body.contentType()}"
                    )
                }
                val result = workstationService.uploadTrack(
                    WorkstationRequest.UploadTrackRequest(
                        partMap = requestBodyMap,
                        trackImg = trackImg,
                        layerSoundFiles = layerSoundFiles,
                        trackSoundFile = trackSoundFile,
                    )
                )

                Log.d("Upload", result.toString())
            }

            onResult(success)
        }

    }


    private fun getAudioLayerInfos(): List<LayerAudioInfo> {
        return tracks.value.map { it.toAudioInfo() }
    }

    fun getMaxUsedBars(layers: List<Layer>): Int {
        return layers.flatMap { layer ->
            layer.patternBlocks.map { it.start + it.length }
        }.maxOrNull() ?: 0
    }

    val bottomBarActions = BottomBarActions(
        onPlayedClicked = {},
        onTrackUploadClicked = { },
        onAddInstrument = {},
    )

    //재생 끝나고 Oboe에서 상태 콜백 받는 함수
    override fun onPlaybackFinished() {
        Log.d("Playback", "🎉 재생이 완료되었습니다!")
        Handler(Looper.getMainLooper()).post {
            stopAudioEngine()
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
}