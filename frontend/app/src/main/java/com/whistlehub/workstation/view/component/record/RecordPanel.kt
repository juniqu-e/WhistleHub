package com.whistlehub.workstation.view.component.record

import android.Manifest
import android.app.Dialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whistlehub.workstation.data.ToastData
import com.whistlehub.workstation.data.rememberToastState
import com.whistlehub.workstation.view.component.CustomToast
import com.whistlehub.workstation.viewmodel.WorkStationViewModel
import java.io.File

@Composable
fun RecordingPanel(viewModel: WorkStationViewModel) {
    val context = LocalContext.current
    var filename by remember { mutableStateOf("") }
    val recordedFile = viewModel.recordedFile
    val isRecording = viewModel.isRecording
    val isRecordingPending = viewModel.isRecordingPending
    val countdown = viewModel.countdown
    val toastState = rememberToastState()
    val verticalScroll = rememberScrollState()
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = File(context.filesDir, "temp_recorded.wav")
            viewModel.startCountdownAndRecord(context, file) {
                viewModel.playRecording(it)
            }
        } else {
            toastState.value = ToastData("녹음 권한이 필요합니다.", Icons.Default.Error, Color(0xFFF44336))
        }
    }

    Column(
        Modifier
            .padding(16.dp)
            .verticalScroll(verticalScroll)
    ) {
        CustomToast(
            toastData = toastState.value,
            onDismiss = { toastState.value = null },
            position = Alignment.Center
        )

        if (countdown > 0) {
            Text("녹음 시작까지 ${countdown}초...", fontSize = 20.sp)
        }

        when {
            isRecording -> {
                Button(onClick = { viewModel.stopRecording() }) {
                    Text(" 녹음 중지")
                }
            }

            isRecordingPending -> {
                Button(onClick = {}, enabled = false) {
                    Text(" 잠시만 기다려주세요...")
                }
            }

            else -> {
                Button(onClick = {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }) {
                    Text("녹음 시작")
                }
            }
        }

        if (recordedFile != null && !isRecording) {
            Spacer(Modifier.height(16.dp))

            Button(onClick = { viewModel.playRecording(recordedFile) }) {
                Text("다시 듣기")
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(value = filename,
                onValueChange = { filename = it },
                label = { Text("파일 이름") })

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.addRecordedLayer(filename)
                    viewModel.toggleAddLayerDialog(false)
                }, enabled = filename.isNotBlank()
            ) {
                Text("레이어로 등록")
            }
        }
    }
}
