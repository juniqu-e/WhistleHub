package com.whistlehub.workstation.view

import android.content.Context
import android.media.MediaMetadataRetriever
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.whistlehub.common.data.remote.dto.request.TrackRequest
import com.whistlehub.common.data.remote.dto.request.WorkstationRequest
import com.whistlehub.common.util.rawWavList
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.workstation.data.InstrumentType
import com.whistlehub.workstation.data.Layer
import com.whistlehub.workstation.viewmodel.WorkStationViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AddLayerDialog(
    context: Context,
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onLayerAdded: (Layer) -> Unit,
) {
    if (!showDialog) return
    val viewModel: WorkStationViewModel = hiltViewModel();

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        var selectedType by remember { mutableStateOf<InstrumentType?>(null) }
        var selectedWavPath by remember { mutableStateOf<String?>(null) }
        Surface(
            modifier = Modifier
                .width(600.dp)
                .height(500.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.8f),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = selectedType,
                    transitionSpec = {
                        if (targetState == null || (initialState != null && (targetState?.ordinal
                                ?: -1) < initialState!!.ordinal)
                        ) {
                            // ← 뒤로 가는 전환
                            (slideInHorizontally { -it } + fadeIn()).togetherWith(
                                slideOutHorizontally { it } + fadeOut())
                        } else {
                            // → 앞으로 가는 전환
                            (slideInHorizontally { it } + fadeIn()).togetherWith(
                                slideOutHorizontally { -it } + fadeOut())
                        }
                    }, modifier = Modifier.fillMaxSize()
                ) { target ->
                    when (target) {
                        null -> {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "악기 선택",
                                    modifier = Modifier.padding(8.dp),
                                    style = Typography.titleLarge,
                                    color = Color.Black
                                )
                                Spacer(Modifier.height(8.dp))
                                LazyVerticalGrid(columns = GridCells.Fixed(2),
                                    modifier = Modifier
                                        .height(300.dp),
                                    content = {
                                        items(InstrumentType.entries) { type ->
                                            Button(
                                                onClick = { selectedType = type },
                                                modifier = Modifier
                                                    .padding(8.dp)
                                                    .height(120.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = type.hexColor,
                                                    contentColor = Color.Black
                                                )
                                            ) {
                                                Text(
                                                    type.label,
                                                    style = Typography.titleMedium,
                                                    textAlign = TextAlign.Center,
                                                    softWrap = false,
                                                    maxLines = 1,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    })
                            }
                        }

                        InstrumentType.SEARCH -> {
                            val searchResults by viewModel.searchTrackResults;
                            val layersOfTrack by viewModel.layersOfSearchTrack;

                            SearchLayerSection(searchResults = searchResults?.payload
                                ?: emptyList(),
                                onSearchClicked = { keyword ->
                                    viewModel.searchTrack(
                                        TrackRequest.SearchTrackRequest(
                                            keyword = keyword, 0, 10, "asc"
                                        )
                                    )
                                },
                                onTrackSelected = { trackId ->
                                    viewModel.addLayerFromSearchTrack(
                                        WorkstationRequest.ImportTrackRequest(
                                            trackId = trackId
                                        ),
                                        context = context,
                                    )
                                },
                                onBack = {
                                    selectedType = null
                                })
                        }

                        else -> {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    target.label,
                                    style = Typography.titleLarge,
                                    color = Color.Black
                                )
                                Spacer(Modifier.height(8.dp))
                                val wavResMap = remember(target) {
                                    getRawWavResMapForInstrument(context, target)
                                }

                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 250.dp)
                                ) {
                                    items(wavResMap.entries.toList()) { (name, resId) ->
                                        Button(
                                            onClick = {
                                                val durationMs = getWavDurationMs(context, resId)
                                                val bpm = 90
                                                val lengthInBars =
                                                    getBarsFromDuration(durationMs, bpm)
                                                val file =
                                                    copyRawToInternal(context, resId, "$name.wav")
                                                val newLayer = Layer(
                                                    id = 0,
                                                    name = selectedType!!.label,
                                                    description = name,
                                                    category = selectedType!!.name,
                                                    length = lengthInBars.toInt(),
                                                    patternBlocks = emptyList(),
                                                    wavPath = file.absolutePath
                                                )
                                                onLayerAdded(newLayer)
                                                onDismiss()
                                            }, modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = target.hexColor,
                                                contentColor = Color.Black
                                            )
                                        ) {
                                            Text(
                                                name.removePrefix("${target.assetFolder}_"),
                                                style = Typography.bodyLarge
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(8.dp))


                                Button(
                                    onClick = { selectedType = null },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFF0F0F0),
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("이전", style = Typography.bodyLarge)
                                }
                                Spacer(Modifier.width(16.dp))
                            }

                        }
                    }
                }
            }
        }
    }
}


fun getRawWavResMapForInstrument(context: Context, type: InstrumentType): Map<String, Int> {
    val res = context.resources
    val pkg = context.packageName

    return rawWavList.filter { it.startsWith(type.assetFolder) }
        .associateWith { res.getIdentifier(it, "raw", pkg) }.filterValues { it != 0 }
}

fun getWavDurationMs(context: Context, resId: Int): Long {
    val afd = context.resources.openRawResourceFd(resId)
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
    val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
    retriever.release()
    return durationStr?.toLongOrNull() ?: 0L
}

fun getBarsFromDuration(durationMs: Long, bpm: Int): Float {
    val beatDurationMs = 60000f / bpm
    val barDurationMs = beatDurationMs * 4
    return durationMs / barDurationMs
}

fun copyRawToInternal(context: Context, resId: Int, outFileName: String): File {
    val outFile = File(context.filesDir, outFileName)
    val input = context.resources.openRawResource(resId)
    val output = FileOutputStream(outFile)
    input.copyTo(output)
    input.close()
    output.close()
    return outFile
}