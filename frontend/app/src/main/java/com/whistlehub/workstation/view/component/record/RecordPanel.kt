package com.whistlehub.workstation.view.component.record

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
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
    val toastState by viewModel.toastMessage.collectAsState()
    val verticalScroll = rememberScrollState()
    val customColors = CustomColors()
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val fileName = "recording_${System.currentTimeMillis()}.wav"
            val file = File(context.filesDir, fileName)
            viewModel.startCountdownAndRecord(context, file) {
                viewModel.playRecording(it)
            }
        } else {
            viewModel.showToast(
                message = "녹음 권한이 필요합니다.",
                icon = Icons.Default.Error,
                color = Color(0xFFF44336)
            )
        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(verticalScroll),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CustomToast(
            toastData = toastState,
            onDismiss = { viewModel.clearToast() },
            position = Alignment.Center
        )

        if (countdown > 0) {
            Text("녹음 시작까지 ${countdown}초...", fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
        // Row to group "녹음 시작" and "다시 듣기" buttons with icons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            when {
                isRecording -> {
                    Button(
                        onClick = { viewModel.stopRecording() },
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFF44336).copy(alpha = 0.3f),
                                        Color.White.copy(alpha = 0.06f)
                                    )
                                )
                            )
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.7f),
                                        Color(0xFFF44336).copy(alpha = 0.5f)
                                    )
                                ),
                                shape = RoundedCornerShape(40.dp)
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = customColors.CommonButtonColor.copy(0.1f),
                            contentColor = customColors.CommonTextColor.copy(0.3f)
                        ),
                    ) {
                        Icon(
                            Icons.Filled.Stop,
                            contentDescription = "Stop Recording",
                            tint = customColors.CommonIconColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "녹음 중지",
                            style = Typography.titleMedium,
                            color = customColors.CommonTextColor
                        )
                    }
                }

                isRecordingPending -> {
                    Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        customColors.CommonButtonColor,
                                        customColors.CommonButtonColor,
                                    )
                                )
                            )
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(0.66f),
                                        Color.White.copy(0.52f)
                                    )
                                ),
                                shape = RoundedCornerShape(40.dp)
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = customColors.CommonButtonColor.copy(0.1f),
                            contentColor = customColors.CommonTextColor.copy(0.3f)
                        ),
                    ) {
                        Icon(
                            Icons.Filled.Error,
                            contentDescription = "Please wait",
                            tint = customColors.CommonIconColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "잠시만 기다려주세요...",
                            color = customColors.CommonTextColor,
                            style = Typography.titleMedium
                        )
                    }
                }

                else -> {
                    Button(
                        onClick = {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF4CAF50).copy(alpha = 0.3f),
                                        Color.White.copy(alpha = 0.06f)
                                    )
                                )
                            )
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.7f),
                                        Color(0xFF4CAF50).copy(alpha = 0.5f)
                                    )
                                ),
                                shape = RoundedCornerShape(40.dp)
                            ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = customColors.CommonButtonColor.copy(0.1f),
                            contentColor = customColors.CommonTextColor.copy(0.3f)
                        ),
                    ) {
                        Icon(
                            Icons.Filled.Mic,
                            contentDescription = "Start Recording",
                            tint = customColors.CommonIconColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "녹음 시작",
                            style = Typography.titleMedium,
                            color = customColors.CommonTextColor
                        )
                    }
                }
            }
        }
        // '다시 듣기' button
        if (recordedFile != null && !isRecording) {
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { viewModel.playRecording(recordedFile) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF2196F3).copy(alpha = 0.3f),
                                Color.White.copy(alpha = 0.06f)
                            )
                        )
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.7f),
                                Color(0xFF2196F3).copy(alpha = 0.5f)
                            )
                        ),
                        shape = RoundedCornerShape(40.dp)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = customColors.CommonButtonColor.copy(0.1f),
                    contentColor = customColors.CommonTextColor.copy(0.3f)
                ),
            ) {
                Icon(
                    Icons.Filled.Replay,
                    contentDescription = "Replay Recording",
                    tint = customColors.CommonIconColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("다시 듣기", style = Typography.titleMedium, color = customColors.CommonTextColor)
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = filename,
                onValueChange = { filename = it },
                label = { Text("이름") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.addRecordedLayer(filename)
                    viewModel.recordFileReset()
                    viewModel.toggleAddLayerDialog(false)
                },
                enabled = filename.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF607D8B).copy(alpha = 0.3f),
                                Color.White.copy(alpha = 0.06f)
                            )
                        )
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.7f),
                                Color(0xFF607D8B).copy(alpha = 0.5f)
                            )
                        ),
                        shape = RoundedCornerShape(40.dp)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = customColors.CommonButtonColor.copy(0.1f),
                    contentColor = customColors.CommonTextColor.copy(0.3f)
                ),
            ) {
                Text(
                    "레이어로 등록",
                    style = Typography.titleMedium, color = customColors.CommonTextColor
                )
            }
        }
    }
}


