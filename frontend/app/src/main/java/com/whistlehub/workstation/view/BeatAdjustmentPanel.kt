package com.whistlehub.workstation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whistlehub.workstation.data.Layer


@Composable
fun BeatAdjustmentPanel(
    layer: Layer,
    onDismiss: () -> Unit,
    onGridClick: (Int) -> Unit,
    onAutoRepeatApply: (startBeat: Int, interval: Int) -> Unit,
) {
    var startBeat by remember { mutableFloatStateOf(0f) }
    var interval by remember { mutableFloatStateOf(4f) }
    // 화면 가운데 정렬 + 너비 제한
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .background(Color(0xFF222222), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "박자 조정", color = Color.White, fontSize = 18.sp)
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            BeatGrid(
                patternBlocks = layer.patternBlocks,
                onClick = onGridClick
            )

            Spacer(modifier = Modifier.height(24.dp))
            // 시작 박자 슬라이더
            Text("시작 박자: ${startBeat.toInt() + 1}", color = Color.White)
            Slider(
                value = startBeat,
                onValueChange = { startBeat = it },
                valueRange = 0f..59f
            )

            Spacer(modifier = Modifier.height(12.dp))
            // 간격 슬라이더
            Text("간격 (Interval): ${interval.toInt()}", color = Color.White)
            Slider(
                value = interval,
                onValueChange = { interval = it },
                valueRange = 1f..60f
            )

            Spacer(modifier = Modifier.height(12.dp))
            // 적용 버튼
            Button(
                onClick = {
                    onAutoRepeatApply(startBeat.toInt(), interval.toInt())
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("패턴 반복 적용")
            }
        }
    }
}

fun Float.format(digits: Int) = "%.${digits}f".format(this)