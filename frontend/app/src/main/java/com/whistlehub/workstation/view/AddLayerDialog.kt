package com.whistlehub.workstation.view

import android.content.Context
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.whistlehub.common.util.rawWavList
import com.whistlehub.workstation.data.InstrumentType
import com.whistlehub.workstation.data.Layer
import java.io.File
import java.io.FileOutputStream

@Composable
fun AddLayerDialog(
    context: Context,
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onLayerAdded: (Layer) -> Unit
) {
    if (!showDialog) return

    Dialog(onDismissRequest = onDismiss) {
        var selectedType by remember { mutableStateOf<InstrumentType?>(null) }
        var selectedWavPath by remember { mutableStateOf<String?>(null) }

        Column(modifier = Modifier.padding(16.dp)) {
            if (selectedType == null) {
                Text("악기 선택")
                Spacer(Modifier.height(8.dp))
                InstrumentType.entries.forEach { type ->
                    Button(onClick = { selectedType = type }) {
                        Text(type.label)
                    }
                }
            } else {
                Text("WAV 선택: ${selectedType!!.label}")
                Spacer(Modifier.height(8.dp))
                val wavResMap = remember(selectedType) {
                    getRawWavResMapForInstrument(context, selectedType!!)
                }

                wavResMap.forEach { (name, resId) ->
                    Button(onClick = {
                        val durationMs = getWavDurationMs(context, resId)
                        val bpm = 90
                        val lengthInBars = getBarsFromDuration(durationMs, bpm)
                        val file = copyRawToInternal(context, resId, "$name.wav")
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
                    }) {
                        Text(name.removePrefix("${selectedType!!.assetFolder}_"))
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row {
                    Button(onClick = { selectedType = null }) {
                        Text("← 이전")
                    }
                    Spacer(Modifier.width(16.dp))
                }
            }
        }
    }
}


fun getRawWavResMapForInstrument(context: Context, type: InstrumentType): Map<String, Int> {
    val res = context.resources
    val pkg = context.packageName

    return rawWavList
        .filter { it.startsWith(type.assetFolder) }
        .associateWith { res.getIdentifier(it, "raw", pkg) }
        .filterValues { it != 0 }
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