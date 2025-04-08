package com.whistlehub.workstation.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whistlehub.common.data.remote.dto.request.TrackRequest
import com.whistlehub.common.data.remote.dto.request.WorkstationRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.AuthResponse
import com.whistlehub.common.data.remote.dto.response.TrackResponse
import com.whistlehub.common.data.remote.dto.response.WorkstationResponse
import com.whistlehub.common.data.repository.TrackService
import com.whistlehub.common.data.repository.WorkstationService
import com.whistlehub.common.util.AudioEngineBridge.getWavDurationSeconds
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
import com.whistlehub.workstation.data.Layer
import com.whistlehub.workstation.data.LayerAudioInfo
import com.whistlehub.workstation.data.PatternBlock
import com.whistlehub.workstation.data.ToastData
import com.whistlehub.workstation.data.UploadMetadata
import com.whistlehub.workstation.data.getCategoryAndColorHex
import com.whistlehub.workstation.data.roundUpToNearestPowerOfTwo
import com.whistlehub.workstation.data.toAudioInfo
import com.whistlehub.workstation.di.AudioLayerPlayer
import com.whistlehub.workstation.di.WorkStationBottomBarProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
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
    private val _showAddLayerDialog = mutableStateOf(false)
    val showAddLayerDialog: State<Boolean> get() = _showAddLayerDialog
    private val _showUploadSheet = mutableStateOf(false)
    val showUploadSheet: State<Boolean> get() = _showUploadSheet
    private val _isUploading = mutableStateOf(false)
    val isUploading: State<Boolean> get() = _isUploading
    private val _tagList = MutableStateFlow<List<AuthResponse.TagResponse>>(emptyList())
    val tagList: StateFlow<List<AuthResponse.TagResponse>> get() = _tagList
    private val _tagPairs = MutableStateFlow<List<Pair<Int, String>>>(emptyList())
    val tagPairs: StateFlow<List<Pair<Int, String>>> get() = _tagPairs

    // ------------ Record ------------------
    var recordedFile by mutableStateOf<File?>(null)
    var isRecording by mutableStateOf(false)
    var isRecordingPending by mutableStateOf(false)
    var countdown by mutableStateOf(3)
    private var mediaPlayer: MediaPlayer? = null
    private var recorder: AudioRecord? = null


    init {
        setCallback(this)
    }

    fun addLayer(newLayer: Layer) {
        val newId = _nextId.intValue  // 현재 ID 가져오기
        _nextId.intValue += 1  // ID 증가
        // ID가 증가된 새로운 Layer 객체 생성
        val layerWithId = newLayer.copy(id = newId)
        // 레이어를 _tracks에 추가
        _tracks.value += layerWithId
    }

    fun deleteLayer(layer: Layer) {
        _tracks.value = _tracks.value.filter { it.id != layer.id }
        Log.d("Play", _tracks.value.toString())
    }

    fun resetLayer(layer: Layer) {
        _tracks.value = _tracks.value.map {
            if (it.id == layer.id) {
                it.copy(
                    length = layer.length,
                    patternBlocks = emptyList()
                )
            } else it
        }
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
                    val duration = getWavDurationMs(localFile)
                    val length = getBarsFromDuration(
                        durationMs = duration,
                        bpm = 120
                    )
                    val bars = layerRes.bars
                    val (category, colorHex) = getCategoryAndColorHex(layerRes.instrumentType)
                    val barsPattern: List<PatternBlock> = if (!bars.isNullOrEmpty()) {
                        bars.map { start ->
                            PatternBlock(start = start, length = length.toInt())
                        }
                    } else {
//                        val interval = 4
//                        val defaultStarts = (0 until 64 step interval)
//                        defaultStarts.map { start ->
//                            PatternBlock(
//                                start = start,
//                                length = 4
//                            )
//                        }
                        listOf(
                            PatternBlock(start = 0, length = length.toInt())
                        )
                    }

                    Layer(
                        name = track.title,
                        description = track.title,
                        category = category,
                        instrumentType = layerRes.instrumentType,
                        colorHex = colorHex,
                        length = length.toInt(),
                        wavPath = localFile.absolutePath,
                        patternBlocks = barsPattern
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

    fun onPlayClicked(onResult: (Boolean) -> Unit = {}) {
        val infos = getAudioLayerInfos();
        // 마디 정보 없는 레이어 체크
        val invalidLayer = tracks.value.find { it.patternBlocks.isEmpty() }
        if (invalidLayer != null) {
            onResult(false)
            return
        }
        val maxUsedBars = getMaxUsedBars(tracks.value)
        if (_isPlaying.value) {
            stopAudioEngine()
        } else {
            stopAudioEngine()
            setLayers(infos, maxUsedBars)
            startAudioEngine()
        }
        _isPlaying.value = !_isPlaying.value
        onResult(true)
    }

    fun onUpload(context: Context, metadata: UploadMetadata, onResult: (ToastData) -> Unit = {}) {
        if (metadata.title.isBlank() || metadata.description.isBlank() || metadata.tags.isEmpty()) {
            onResult(ToastData("제목, 설명, 태그를 모두 입력해주세요.", Icons.Default.Error, Color(0xFFF44336)))
            return
        }
        val fileName = metadata.title
        val safeFileName = if (fileName.endsWith(".wav")) fileName else "$fileName.wav"
        val mix = File(context.filesDir, safeFileName)
        var response: ApiResponse<Int>

        viewModelScope.launch {
            _isUploading.value = true
            val infos = getAudioLayerInfos();
            val maxUsedBars = getMaxUsedBars(tracks.value)
            setLayers(infos, maxUsedBars)
            val totalFrames = calculateTotalFrames(
                maxUsedBars = maxUsedBars,
                bpm = 120.0f,
                sampleRate = 44100,
            )
            val success = renderMixToWav(mix.absolutePath, totalFrames)

            if (!success) {
                _isUploading.value = false
                onResult(ToastData("음원 합성에 실패하였습니다.", Icons.Default.Error, Color(0xFFF44336)))
                return@launch
            } else {
                //MultiPart
                val ids = tracks.value.joinToString(",") { it.id.toString() }
                val names = tracks.value.joinToString(",") { it.name }
                val instrumentTypes =
                    tracks.value.joinToString(",") { it.instrumentType.toString() }
                val duration = getWavDurationSeconds(mix.absolutePath).toInt().toString()
                val bars: List<List<Int>> = tracks.value.map { layer ->
                    layer.patternBlocks.map { it.start }
                }
                val barsJsonString = bars.joinToString(prefix = "[", postfix = "]") { block ->
                    block.joinToString(prefix = "[", postfix = "]", separator = ",")
                }
                val requestBodyMap = hashMapOf(
                    "title" to createRequestBody(fileName), //
                    "description" to createRequestBody(metadata.description),
                    "duration" to createRequestBody(duration),
                    "visibility" to createRequestBody(metadata.visibility.toString()), //
                    "tags" to createRequestBody(metadata.tags.joinToString(",")), //
                    "sourceTracks" to createRequestBody(ids),
                    "layerName" to createRequestBody(names),
                    "instrumentType" to createRequestBody(instrumentTypes),
                    "barsJson" to createRequestBody(barsJsonString)
                )

                Log.d("Request", requestBodyMap.toString())
                val trackImg = null
                val trackSoundFile = createMultipart(mix, "trackSoundFile")
                val layerSoundFiles = tracks.value.map { layer ->
                    Log.d("Upload", File(layer.wavPath).toString())
                    createMultipart(File(layer.wavPath), "layerSoundFiles")
                }

                requestBodyMap.forEach { (key, value) ->
                    Log.d("Upload", "$key => ${value.peekContent()}")
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
                response = workstationService.uploadTrack(
                    WorkstationRequest.UploadTrackRequest(
                        partMap = requestBodyMap,
                        trackImg = trackImg,
                        layerSoundFiles = layerSoundFiles,
                        trackSoundFile = trackSoundFile,
                    )
                )

                if (response.code != "SU") {
                    _isUploading.value = false
                    onResult(
                        ToastData(
                            "믹스를 서버 저장에 실패하였습니다.",
                            Icons.Default.Error,
                            Color(0xFFF44336)
                        )
                    )
                    return@launch
                }
            }
            _isUploading.value = false
            onResult(
                ToastData("믹스를 서버 저장에 성공하였습니다.", Icons.Default.CheckCircle, Color(0xFF4CAF50))
            )
        }
    }

    suspend fun getTagList() {
        val response = trackService.getTagRecommendation().payload!!
        _tagList.value = response
        _tagPairs.value = response.map { it.id to it.name }
    }

    private fun getWavDurationMs(file: File): Long {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(file.absolutePath)
        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        retriever.release()
        return durationStr?.toLongOrNull() ?: 0L
    }

    private fun getBarsFromDuration(durationMs: Long, bpm: Int): Float {
        val beatDurationMs = 60000f / bpm
        val barDurationMs = beatDurationMs * 4
        val calculatedLength = durationMs / barDurationMs
        return roundUpToNearestPowerOfTwo(calculatedLength).toFloat()
    }

    private fun getAudioLayerInfos(): List<LayerAudioInfo> {
        return tracks.value.map { it.toAudioInfo() }
    }

    private fun getMaxUsedBars(layers: List<Layer>): Int {
        return layers.flatMap { layer ->
            layer.patternBlocks.map { it.start + it.length }
        }.maxOrNull() ?: 0
    }

    private fun calculateTotalFrames(maxUsedBars: Int, bpm: Float, sampleRate: Int): Int {
        val secondsPerBar = (60f / bpm) * 4f
        val totalSeconds = maxUsedBars * secondsPerBar
        return (totalSeconds * sampleRate).toInt()
    }

    fun toggleUploadSheet(show: Boolean) {
        _showUploadSheet.value = show
    }

    fun toggleAddLayerDialog(show: Boolean) {
        _showAddLayerDialog.value = show
    }

    fun RequestBody.peekContent(): String {
        val buffer = okio.Buffer()
        this.writeTo(buffer)
        return buffer.readUtf8()
    }

    val bottomBarActions = BottomBarActions(
        onPlayedClicked = {},
        onTrackUploadClicked = { },
        onAddInstrument = {},
        onUploadButtonClick = {
            toggleUploadSheet(true)
        }
    )

    //재생 끝나고 Oboe에서 상태 콜백 받는 함수
    override fun onPlaybackFinished() {
        Log.d("Playback", "🎉 재생이 완료되었습니다!")
        Handler(Looper.getMainLooper()).post {
            stopAudioEngine()
            _isPlaying.value = false
        }
    }

    fun startCountdownAndRecord(context: Context, file: File, onComplete: (File) -> Unit) {
        isRecordingPending = true
        viewModelScope.launch {
            for (i in 3 downTo 1) {
                countdown = i
                delay(1000)
            }
            countdown = 0
            startRecording(context, file, onComplete)
        }
    }

    private fun startRecording(context: Context, file: File, onComplete: (File) -> Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            Log.d("Record", hasPermission.toString())
            return
        }

        isRecording = true
        isRecordingPending = false
        val sampleRate = 44100
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        if (recorder!!.state != AudioRecord.STATE_INITIALIZED) {
            Log.e("Record", "AudioRecord 초기화 실패")
            isRecording = false
            return
        }
        val pcmStream = ByteArrayOutputStream()
        val buffer = ByteArray(bufferSize)

        viewModelScope.launch(Dispatchers.IO) {
            recorder?.startRecording()
            while (isRecording && recorder?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                val read = recorder?.read(buffer, 0, buffer.size) ?: break
                if (read > 0) {
                    Log.d("Record", "read: $read / buffer.first: ${buffer[0]}")
                    pcmStream.write(buffer, 0, read)
                }
            }

            recorder?.stop()
            recorder?.release()
            recorder = null
            isRecording = false
            val channels = 1
            val bitsPerSample = 16
            val byteRate = sampleRate * channels * (bitsPerSample / 8)
            val wavStream = FileOutputStream(file)
            val pcmData = pcmStream.toByteArray()

            writeWavHeader(wavStream, pcmData.size.toLong(), sampleRate, 1, byteRate)
            wavStream.write(pcmData)
            wavStream.close()

            recordedFile = file
            onComplete(file)
        }
    }

    fun stopRecording() {
        isRecording = false
    }

    fun playRecording(file: File) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            prepare()
            start()
        }
    }

    fun stopPlayback() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun writeWavHeader(
        out: FileOutputStream,
        audioLen: Long,
        sampleRate: Int,
        channels: Int,
        byteRate: Int
    ) {
        val totalLen = audioLen + 36
        val header = ByteArray(44)

        fun writeInt(offset: Int, value: Int) {
            ByteBuffer.wrap(header, offset, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(value)
        }

        fun writeShort(offset: Int, value: Short) {
            ByteBuffer.wrap(header, offset, 2).order(ByteOrder.LITTLE_ENDIAN).putShort(value)
        }

        "RIFF".toByteArray().copyInto(header, 0)
        writeInt(4, totalLen.toInt())
        "WAVE".toByteArray().copyInto(header, 8)
        "fmt ".toByteArray().copyInto(header, 12)
        writeInt(16, 16)
        writeShort(20, 1)
        writeShort(22, channels.toShort())
        writeInt(24, sampleRate)
        writeInt(28, byteRate)
        writeShort(32, (channels * 2).toShort())
        writeShort(34, 16)
        "data".toByteArray().copyInto(header, 36)
        writeInt(40, audioLen.toInt())

        out.write(header)
    }

    fun addRecordedLayer(name: String) {
        recordedFile?.let { file ->
            val duration = getWavDurationMs(file)
            val length = getBarsFromDuration(
                durationMs = duration,
                bpm = 120
            )
            val layer = Layer(
                id = 0,
                name = name,
                description = "녹음",
                category = "RECORDED",
                instrumentType = 0,
                length = length.toInt(),
                patternBlocks = emptyList(),
                wavPath = file.absolutePath
            )
            addLayer(layer)
        }
    }
}