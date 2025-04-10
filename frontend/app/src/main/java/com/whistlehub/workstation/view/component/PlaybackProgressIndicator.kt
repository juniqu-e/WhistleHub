package com.whistlehub.workstation.view.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun WaveformWithProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Float,                  // 0f ~ 1f 값으로 진행률 표시
    isPlaying: Boolean,               // 재생 중인 상태인지 확인
    primaryColor: Color,
    secondaryColor: Color,
    waveformPoints: List<Float> = emptyList()  // 선택사항: 파형 데이터를 전달받을 수 있음
) {
    var circleCenter by remember { mutableStateOf(Offset.Zero) }
    val infiniteTransition = rememberInfiniteTransition()
    // 애니메이션 (펄스 효과) - 재생 중일 때만 적용
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = if (isPlaying) 1.05f else 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500),
            repeatMode = RepeatMode.Reverse
        )
    ) // 빛나는 효과: 재생 중일 때만 색상 변화
    val color = if (isPlaying) {
        Color.Yellow // 재생 중일 때 빛나는 노란색
    } else {
        Color.Magenta // 정지 중일 때 보통 색상
    }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                )
        ) {
            // 캔버스 크기 및 중심 계산
            val width = size.width
            val height = size.height
            // 원 테두리 두께 (캔버스 너비에 비례)
            val circleThickness = width / 50f
            val radius = min(width, height) / 2.5f
            circleCenter = Offset(width / 2f, height / 2f)
            // 바깥쪽 눈금(틱) 그리기를 위해 outerRadius 계산
            val outerRadius = radius + circleThickness / 2f
            // 눈금(틱)과 원 사이의 간격
            val tickGap = 15f
            // 1. 원형 배경 그리기 (그라데이션 효과)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.45f),
                        secondaryColor.copy(alpha = 0.15f)
                    )
                ),
                radius = radius,
                center = circleCenter
            )
            // 2. 원 테두리
            drawCircle(
                color = secondaryColor,
                radius = radius,
                center = circleCenter,
                style = Stroke(width = circleThickness)
            )
            // 진행률 아크 (진행된 부분을 원호로 표시)
            drawArc(
                color = primaryColor,
                startAngle = -90f,  // 오른쪽에서 시작
                sweepAngle = progress * 360f,  // 진행률에 따라 아크 그리기
                useCenter = false,
                style = Stroke(width = circleThickness, cap = StrokeCap.Round),
                size = Size(radius * 2f, radius * 2f),
                topLeft = Offset((width - radius * 2f) / 2f, (height - radius * 2f) / 2f)
            )

            if (waveformPoints.isNotEmpty()) {
                for (i in waveformPoints.indices) {
                    val amplitude = waveformPoints[i]
                    val waveColor =
                        if (amplitude >= 0) primaryColor else primaryColor.copy(alpha = 0.3f)
                    // 각 눈금의 절대 각도 계산 (0°에서 시작하여 360°까지 고르게)
                    val tickAngleDeg = i * 360f / waveformPoints.size.toFloat()
                    val tickAngleRad = Math.toRadians(tickAngleDeg.toDouble())
                    val tickStartRadius = radius + circleThickness   // 테두리 바깥 (조정 가능)
                    val tickEndRadius =
                        tickStartRadius + (33 + (abs(amplitude) * abs(amplitude)) * 25)
                    // 눈금 시작점: 원의 중심에서 반지름만큼 떨어진 점
                    val startX = circleCenter.x + tickStartRadius * cos(tickAngleRad).toFloat()
                    val startY = circleCenter.y + tickStartRadius * sin(tickAngleRad).toFloat()
                    // 눈금 끝점: 시작점에서 고정 길이(tickGap)만큼 연장
                    val endX = circleCenter.x + tickEndRadius * cos(tickAngleRad).toFloat()
                    val endY = circleCenter.y + tickEndRadius * sin(tickAngleRad).toFloat()

                    drawLine(
                        color = waveColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
        }
    }
}
//    Canvas(
//        modifier = Modifier
//            .size(180.dp)
//            .graphicsLayer(scaleX = scale, scaleY = scale) // 펄스 효과 적용
//    ) {
//        val radius = size.minDimension / 2f
//        val centerX = size.width / 2f
//        val centerY = size.height / 2f
//        val numPoints = waveformPoints.size
//        val angleStep = 360f / numPoints // 각 파형 점들 사이의 각도 계산
//
//        waveformPoints.forEachIndexed { index, amplitude ->
//            val angle = Math.toRadians((index * angleStep).toDouble()).toFloat() // 각 점의 각도
//            val x = centerX + (radius * amplitude * cos(angle.toDouble())).toFloat()
//            val y = centerY + (radius * amplitude * sin(angle.toDouble())).toFloat()
//            // 선으로 파형 점을 연결
//            drawLine(
//                start = Offset(centerX, centerY),  // 원의 중심
//                end = Offset(x, y),  // 계산된 파형 점
//                color = color.copy(alpha = amplitude),  // 진폭에 따라 색상 투명도 변경
//                strokeWidth = 2.dp.toPx()
//            )
//        }
//    }




