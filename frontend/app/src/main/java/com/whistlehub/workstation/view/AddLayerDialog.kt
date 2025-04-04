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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.navigation.NavController
import com.whistlehub.common.data.remote.dto.request.TrackRequest
import com.whistlehub.common.data.remote.dto.request.WorkstationRequest
import com.whistlehub.common.util.rawWavList
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.workstation.data.InstrumentType
import com.whistlehub.workstation.data.Layer
import com.whistlehub.workstation.data.LayerButtonType
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
    viewModel: WorkStationViewModel,
    navController: NavController,
) {
    if (!showDialog) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        var selectedType by remember { mutableStateOf<LayerButtonType?>(null) }
        var selectedWavPath by remember { mutableStateOf<String?>(null) }
        Surface(
            modifier = Modifier
                .width(600.dp)
                .height(300.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.Gray,
            tonalElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
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
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                LazyVerticalGrid(columns = GridCells.Fixed(2),
                                    modifier = Modifier
                                        .height(300.dp),
                                    verticalArrangement = Arrangement.Center,
                                    content = {
                                        items(LayerButtonType.entries) { type ->
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

                        LayerButtonType.SEARCH -> {
                            LaunchedEffect(Unit) {
                                onDismiss()
                                navController.navigate("search")
                            }
//                            val searchResults by viewModel.searchTrackResults.collectAsState();
//                            val layersOfTrack by viewModel.layersOfSearchTrack;
//
//                            SearchLayerSection(searchResults = searchResults?.payload
//                                ?: emptyList(),
//                                onSearchClicked = { keyword ->
//                                    viewModel.searchTrack(
//                                        TrackRequest.SearchTrackRequest(
//                                            keyword = keyword, 0, 10, "asc"
//                                        )
//                                    )
//                                },
//                                onTrackSelected = { trackId ->
//                                    viewModel.addLayerFromSearchTrack(
//                                        WorkstationRequest.ImportTrackRequest(
//                                            trackId = trackId
//                                        ),
//                                        context = context,
//                                    )
//                                },
//                                onBack = {
//                                    selectedType = null
//                                })
                        }

                        LayerButtonType.RECORD -> {
                            val searchResults by viewModel.searchTrackResults.collectAsState();
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