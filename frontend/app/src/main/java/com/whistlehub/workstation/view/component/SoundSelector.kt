package com.whistlehub.workstation.view.component

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.whistlehub.R
import java.io.File

@Composable
fun SoundSelector(
    layerId: Int,
    onPathSelected: (Int, String) -> Unit
) {
    val context = LocalContext.current
    // res/raw 에 넣은 파일들 여기에
    val wavOptions = listOf(
        "drum1" to R.raw.drum_90bpm_4a4,
        "drum2" to R.raw.drum_100bpm_4a4
    )
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(onClick = { expanded = true }) {
            Text("앱 내 WAV 선택")
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            wavOptions.forEach { (name, resId) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        val path = copyRawToInternal(context, resId, "layer_${layerId}_$name.wav")
                        onPathSelected(layerId, path.absolutePath)
                        expanded = false
                    }
                )
            }
        }
    }
}

