package com.whistlehub.workstation.view.component


import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin


@Composable
fun BPMIndicator(
    modifier: Modifier = Modifier,
    initialValue: Int,             // 실제 BPM 값
    primaryColor: Color,
    secondaryColor: Color,
    minValue: Int = 90,            // 최소 BPM (ex. 90)
    maxValue: Int = 200,           // 최대 BPM (ex. 200)
    onPositionChange: (Int) -> Unit
) {
    // 실제 BPM 값을 보관 (범위 내 유지)
    var positionValue by remember { mutableStateOf(initialValue.coerceIn(minValue, maxValue)) }
    // 드래그 시작 시의 터치 각도 (0~360°)
    var dragStartedAngle by remember { mutableStateOf(0f) }
    // 드래그 시작 전의 BPM 값 (실제값)
    var oldPositionValue by remember { mutableStateOf(positionValue) }
    // 드래그 활성화 여부 (원 테두리 근처에서만 허용)
    var isDragging by remember { mutableStateOf(false) }
    // 캔버스 중심 좌표 저장
    var circleCenter by remember { mutableStateOf(Offset.Zero) }
    // 전체 범위 (예: 200 - 90 = 110)
    val range = maxValue - minValue
    // 상대값: 실제 값에서 minValue를 뺀 값 (0부터 시작)
    val relativeValue = positionValue - minValue

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            // 캔버스가 그려진 후 중심과 반지름 계산
                            val canvasSize = size
                            // 원의 반지름: 캔버스 짧은 쪽에 비례 (여백을 고려)
                            val radius = min(canvasSize.width, canvasSize.height) / 2.5f
                            circleCenter = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
                            // 터치 시작 시, 중심에서의 거리를 계산하여 허용 범위(반지름 ± threshold) 내인지 확인
                            val distance = (offset - circleCenter).getDistance()
                            val threshold = 25f  // 허용 오차 (조절 가능)
                            isDragging = distance in (radius - threshold)..(radius + threshold)

                            if (isDragging) {
                                // 터치 위치의 절대 각도 계산 (0~360°) – 보정 없이 사용
                                dragStartedAngle = calculateAngle(circleCenter, offset)
                            }
                        },
                        onDrag = { change, _ ->
                            if (!isDragging) return@detectDragGestures
                            // 현재 터치 위치의 절대 각도 계산
                            val touchAngle = calculateAngle(circleCenter, change.position)
                            // 현재 BPM값의 상대 각도: (oldPositionValue - minValue) * 360 / range
                            val currentAngle = (oldPositionValue - minValue) * 360f / range
                            // 변화 각도 = 터치 각도의 변화값
                            val angleDiff = touchAngle - currentAngle
                            // 한 BPM 단위가 차지하는 각도: 전체 360도를 range로 나눔
                            val stepAngle = 360f / range
                            // 변화된 BPM 단위 (정수 반올림)
                            val delta = (angleDiff / stepAngle).roundToInt()
                            // 새로운 실제 BPM 값 (oldPositionValue + delta), 범위 내 유지
                            positionValue = (oldPositionValue + delta).coerceIn(minValue, maxValue)
                        },
                        onDragEnd = {
                            oldPositionValue = positionValue
                            onPositionChange(positionValue)
                            isDragging = false
                        }
                    )
                }
        ) {
            // 캔버스 크기 및 중심 계산
            val width = size.width
            val height = size.height
            // 원 테두리 두께 (캔버스 너비에 비례)
            val circleThickness = width / 50f
            // 원의 반지름: 캔버스의 짧은 쪽의 1/2.5 (여백 고려)
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
            // 3. 진행 Arc 그리기
            // 진행은 상대값(relativeValue)을 전체 범위(range)로 나눈 비율에 360°를 곱하여 계산함.
            drawArc(
                color = primaryColor,
                startAngle = 0f, // 0도 기준 (즉, 오른쪽부터 시작)
                sweepAngle = 360f * (relativeValue / range.toFloat()),
                useCenter = false,
                style = Stroke(width = circleThickness, cap = StrokeCap.Round),
                size = Size(radius * 2f, radius * 2f),
                topLeft = Offset((width - radius * 2f) / 2f, (height - radius * 2f) / 2f)
            )
            // 4. 눈금 (Tick marks)
            // 눈금은 0부터 range(= maxValue - minValue)까지 반복합니다.
            for (i in 0..range) {
                // 각 눈금의 색상: 현재 상대값보다 작으면 primaryColor, 그렇지 않으면 alpha 낮춰서 표시
                val tickColor =
                    if (i < relativeValue) primaryColor else primaryColor.copy(alpha = 0.3f)
                // 각 눈금의 절대 각도 계산 (0°에서 시작하여 360°까지 고르게)
                val tickAngleDeg = i * 360f / range.toFloat()
                val tickAngleRad = Math.toRadians(tickAngleDeg.toDouble())
                val tickStartRadius = radius + circleThickness   // 테두리 바깥 (조정 가능)
                val tickEndRadius = tickStartRadius + 16f           // 길이(8픽셀 등)
                // 눈금 시작점: 원의 중심에서 반지름만큼 떨어진 점
                val startX = circleCenter.x + tickStartRadius * cos(tickAngleRad).toFloat()
                val startY = circleCenter.y + tickStartRadius * sin(tickAngleRad).toFloat()
                // 눈금 끝점: 시작점에서 고정 길이(tickGap)만큼 연장
                val endX = circleCenter.x + tickEndRadius * cos(tickAngleRad).toFloat()
                val endY = circleCenter.y + tickEndRadius * sin(tickAngleRad).toFloat()
                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 1.dp.toPx()
                )
            }
            // 5. 중앙 텍스트: 현재 BPM 값 표시
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawText(
                    "$positionValue BPM",
                    circleCenter.x,
                    circleCenter.y + 45.dp.toPx() / 4.5f,
                    Paint().apply {
                        textSize = 20.sp.toPx()
                        textAlign = Paint.Align.CENTER
                        color = Color.White.toArgb()
                        isFakeBoldText = true
                    }
                )
            }
        }
    }
}

/**
 * calculateAngle
 *
 * 주어진 중심(center)과 터치한 점(point) 사이의 절대각(0 ~ 360°)을 계산합니다.
 * - atan2를 사용하여 기본 각도를 계산(기본 0°는 3시 방향)
 * - 음수인 경우 360°를 더해 0~360 범위로 정규화합니다.
 */
private fun calculateAngle(center: Offset, point: Offset): Float {
    val dx = point.x - center.x
    val dy = point.y - center.y
    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
    if (angle < 0f) angle += 360f
    return angle
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    BPMIndicator(
        modifier = Modifier
            .size(200.dp)
            .background(Color.Gray),
        initialValue = 120,
        primaryColor = Color.Cyan,
        secondaryColor = Color.DarkGray,
        minValue = 90,
        maxValue = 200,
        onPositionChange = { newBpm ->
            println("BPM changed to $newBpm")
        }
    )
}