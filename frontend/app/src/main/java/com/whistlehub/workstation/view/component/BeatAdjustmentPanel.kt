package com.whistlehub.workstation.view.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.workstation.data.Layer


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeatAdjustmentPanel(
    layer: Layer,
    onDismiss: () -> Unit,
    onGridClick: (Int) -> Unit,
    onAutoRepeatApply: (startBeat: Int, interval: Int) -> Unit,
) {
    var startBeat by remember { mutableFloatStateOf(0f) }
    var interval by remember { mutableFloatStateOf(1f) }
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
                Text(text = "마디 조정", color = Color.White, fontSize = 18.sp)
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
                onClick = onGridClick,
                accentColor = Color(android.graphics.Color.parseColor(layer.colorHex))
            )

            Spacer(modifier = Modifier.height(24.dp))
            // 시작 마디 슬라이더
//            Text("시작 마디: ${startBeat.toInt() + 1}", color = Color.White)
//            Slider(
//                value = startBeat,
//                onValueChange = { startBeat = it },
//                valueRange = 0f..59f
//            )

            StartBarSelector(
                label = "시작 마디",
                value = startBeat,
                onValueChange = { startBeat = it },
                accentColor = Color(android.graphics.Color.parseColor(layer.colorHex))
            )


            Spacer(modifier = Modifier.height(12.dp))

            RepeatBarSelector(
                label = "반복 간격",
                value = interval,
                onValueChange = { interval = it },
                accentColor = Color(android.graphics.Color.parseColor(layer.colorHex))
            )


//            BeatSlider(
//                label = "시작 마디",
//                value = startBeat,
//                onValueChange = { startBeat = it },
//                valueRange = 0f..59f,
//            )
//
//
//            // 간격 슬라이더
//            BeatSlider(
//                label = "반복 간격",
//                value = interval,
//                onValueChange = { interval = it },
//                valueRange = 1f..60f,
//            )
            Spacer(modifier = Modifier.height(12.dp))
            // 적용 버튼
            Button(
                onClick = {
                    onAutoRepeatApply(startBeat.toInt(), interval.toInt())
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(android.graphics.Color.parseColor(layer.colorHex)).copy(alpha = 0.3f),
                                Color.White.copy(alpha = 0.06f)
                            )
                        )
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.7f),
                                Color(android.graphics.Color.parseColor(layer.colorHex)).copy(alpha = 0.5f)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CustomColors().CommonButtonColor.copy(
                        0.1f
                    ),
                    contentColor = CustomColors().CommonTextColor
                )

            ) {
                Text("마디 반복 적용")
            }
        }
    }
}

fun Float.format(digits: Int) = "%.${digits}f".format(this)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeatSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    track: @Composable (sliderState: SliderState) -> Unit = { sliderState ->
        BeatSliderDefaults.Track(sliderState = sliderState)
    },
) {
    Column {
        Text(text = "$label: ${value.toInt()}", color = Color.White)

        Slider(value = value, onValueChange = onValueChange, valueRange = valueRange,
            thumb = {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(CustomColors().Mint400)
                )
            },
            track = { track(it) }
        )
    }
}

object BeatSliderDefaults {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Track(
        sliderState: SliderState,
        modifier: Modifier = Modifier,
        trackColor: Color = TrackColor,
        progressColor: Color = PrimaryColor,
        height: Dp = 8.dp,
        shape: Shape = CircleShape
    ) {
        Box(
            modifier = modifier
                .track(height = height, shape = shape)
                .background(trackColor)
        ) {
            Box(
                modifier = Modifier
                    .progress(
                        sliderState = sliderState,
                        height = height,
                        shape = shape
                    )
                    .background(progressColor)
            )
        }
    }
}


fun Modifier.track(
    height: Dp = 8.dp,
    shape: Shape = CircleShape
) = this
    .fillMaxWidth()
    .heightIn(min = height)
    .clip(shape)

@OptIn(ExperimentalMaterial3Api::class)
fun Modifier.progress(
    sliderState: SliderState,
    height: Dp = 8.dp,
    shape: Shape = CircleShape
) = this
    .fillMaxWidth(fraction = (sliderState.value - sliderState.valueRange.start) / (sliderState.valueRange.endInclusive - sliderState.valueRange.start))
    .heightIn(min = height)
    .clip(shape)

val PrimaryColor = Color(0xFF6650a4)
val TrackColor = Color(0xFFE7E0EC)