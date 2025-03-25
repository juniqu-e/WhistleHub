package com.whistlehub.workstation.view

import android.app.Activity
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.workstation.data.Layer
import com.whistlehub.workstation.viewmodel.WorkStationViewModel

@Composable
fun WorkStationScreen(navController: NavController) {
    val activity = LocalActivity.current as? Activity
    val viewModel: WorkStationViewModel = hiltViewModel()
    val tracks by viewModel.tracks.collectAsState()
    val verticalScrollState = rememberScrollState()
    val bottomBarActions = viewModel.bottomBarActions.copy(
        onExitClicked = {
            navController.popBackStack()
            Log.d("Exit", "EXIT")
        }
    )
    // Immersive mode (fullscreen)
    LaunchedEffect(Unit) {
        activity?.window?.let { window ->
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
    // Restore system bars when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            activity?.window?.let { window ->
                val controller = WindowInsetsControllerCompat(window, window.decorView)
                controller.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.displayCutout)
            .background(Color.Black)
    ) {
        //좌측 악기
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LayerPanel(
                tracks = tracks,
                verticalScrollState = verticalScrollState,
                modifier = Modifier.fillMaxWidth(),
                onAddInstrument = {
                    viewModel.addLayer()
                },
                onDeleteLayer = {
                    viewModel.deleteLayer(it)
                },
                onResetLayer = {
                    //믹싱 옵션 초기화
                }
            )
        }

        viewModel.bottomBarProvider.WorkStationBottomBar(bottomBarActions)
    }
}

@Composable
fun LayerPanel(
    tracks: List<Layer>,
    onAddInstrument: () -> Unit,
    onDeleteLayer: (Layer) -> Unit,
    onResetLayer: (Layer) -> Unit,
    verticalScrollState: ScrollState,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(Color.Gray)
            .verticalScroll(verticalScrollState)
            .padding(8.dp)
    ) {
        tracks.forEach { layer ->
            LayerItem(
                layer = layer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                onDelete = onDeleteLayer,
                onReset = onResetLayer,
            )
            Spacer(modifier = Modifier.heightIn(8.dp))
        }
        // + 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(8.dp)
                .background(Color.DarkGray, RoundedCornerShape(6.dp))
                .clickable { onAddInstrument() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Instrument",
                tint = Color(0xFF4ECCA3),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun LayerItem(
    layer: Layer,
    modifier: Modifier,
    onDelete: (Layer) -> Unit,
    onReset: (Layer) -> Unit
) {
    val bgColor = getTrackColor(layer)
    val textColor = if (bgColor.luminance() > 0.5f) Color.Black else Color.White
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp), // 하드웨어 고려 살짝 줄이기 (양쪽 padding)
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 왼쪽 카테고리 박스
        Box(
            modifier = Modifier
                .background(bgColor, RoundedCornerShape(6.dp))
                .size(80.dp)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = layer.name,
                    color = Color.Black,
                    fontSize = 14.sp
                )
            }
        }
        //구분 패딩
        Spacer(modifier = Modifier.width(8.dp))
        //오른쪽 레이어
        Row(
            modifier = Modifier
                .height(80.dp)
                .weight(1f)
                .background(bgColor, RoundedCornerShape(6.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = layer.name,
                    style = Typography.bodyLarge,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = layer.description,
                    style = Typography.bodyMedium,
                    color = textColor
                )
            }
            // 레이어 메뉴 버튼
            Box() {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Options",
                    tint = textColor,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            menuExpanded = true
                        }
                )

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    //삭제 이벤트
                    DropdownMenuItem(
                        text = { Text("레이어 삭제") },
                        onClick = {
                            menuExpanded = false
                            onDelete(layer)
                        }
                    )
                    //초기화 이벤트
                    DropdownMenuItem(
                        text = { Text("믹싱 초기화") },
                        onClick = {
                            menuExpanded = false
                            onReset(layer)
                        }
                    )
                }
            }
        }
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SliderMinimalExample() {
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    Column {
        Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it },
            thumb = {
                Box(
                    modifier = Modifier
                        .size(24.dp)              // 원하는 thumb 크기
                        .clip(CircleShape)        // 원형으로 클리핑
                        .background(Color.Cyan)   // thumb 색상
                )
            },
            track = { sliderPositions ->
                // 전체 막대 높이
                val trackHeight = 6.dp
                // 활성 범위의 끝 지점(0f~1f 사이)
                val fraction = sliderPositions.value

                Box(
                    modifier = Modifier
                        .fillMaxWidth()             // 슬라이더 전체 폭
                        .height(trackHeight)
                        .clip(RoundedCornerShape(percent = 50)) // 양 끝이 둥근 막대
                        .background(Color.LightGray)             // 비활성 구간 색상
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)  // value 비율만큼만 채우기
                        .height(6.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(Color.Cyan)     // 활성 구간 색상
                )
            }
        )
        Text(text = sliderPosition.toString())
    }
}

@Composable
fun AudioProgressBar(modifier: Modifier, backgroundColor: Color, progressColor: Color, height: Dp) {
    Box(
        modifier = modifier
            .height(height)
            .background(backgroundColor, RoundedCornerShape(4.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .background(progressColor, RoundedCornerShape(4.dp))
        )
    }
}

@Composable
fun getTrackColor(layer: Layer): Color {
    layer.colorHex?.let {
        return Color(android.graphics.Color.parseColor(it))
    }

    return when (layer.category.uppercase()) {
        "DRUM" -> Color(0xFFFFEE58) // 연한 노랑
        "BASS" -> Color(0xFF9575CD) // 보라
        "OTHERS" -> Color(0xFF80CBC4) // 민트
        else -> Color(0xFFBDBDBD) // 기본 회색
    }
}


