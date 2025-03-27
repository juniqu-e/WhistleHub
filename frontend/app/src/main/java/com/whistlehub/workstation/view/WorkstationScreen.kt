package com.whistlehub.workstation.view

import android.app.Activity
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
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
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.whistlehub.workstation.viewmodel.WorkStationViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
    val selectedLayerId = remember { mutableStateOf<Int?>(null) }
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
        // 원래 orientation 저장
        val originalOrientation = activity?.requestedOrientation
        // landscape로 강제 변경
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            // WorkStation 벗어날때 원래 orientation 복구
            activity?.requestedOrientation = originalOrientation
                ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
     */
    Box(
        modifier =
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.displayCutout)
            .background(Color.LightGray)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
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
                },
                onBeatAdjustment = { layer ->
//                    beatAdjustmentLayer = layer
                    selectedLayerId.value = layer.id
                }
            )
        }

        viewModel.bottomBarProvider.WorkStationBottomBar(bottomBarActions)
        val selectedLayer = tracks.firstOrNull { it.id == selectedLayerId.value }
        selectedLayer?.let { layer ->
            ModalBottomSheet(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.displayCutout)
                    .padding(horizontal = 16.dp),
                onDismissRequest = { selectedLayerId.value = null }
            ) {
                BeatAdjustmentPanel(
                    layer = layer,
                    onDismiss = { selectedLayerId.value = null },
                    onGridClick = { index ->
                        viewModel.toggleBeat(layer.id, index)
                    },
                    onAutoRepeatApply = { start, interval ->
                        viewModel.applyPatternAutoRepeat(selectedLayer.id, start, interval)
                    }
                )
            }
        }
//        // 박자 조정 바텀시트 표시
//        if (beatAdjustmentLayer != null) {
//            ModalBottomSheet(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .windowInsetsPadding(WindowInsets.displayCutout)
//                    .padding(horizontal = 16.dp),
//                onDismissRequest = { beatAdjustmentLayer = null }
//            ) {
//                BeatAdjustmentPanel(
//                    layer = beatAdjustmentLayer!!,
//                    onDismiss = { beatAdjustmentLayer = null },
//                    onGridClick = { index ->
//                        viewModel.toggleBeat(beatAdjustmentLayer!!.id, index)
//                    }
//                )
//            }
//        }
    }
}

@Composable
fun LayerPanel(
    tracks: List<Layer>,
    onAddInstrument: () -> Unit,
    onDeleteLayer: (Layer) -> Unit,
    onResetLayer: (Layer) -> Unit,
    onBeatAdjustment: (Layer) -> Unit,
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
                onBeatAdjustment = onBeatAdjustment,
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

@Composable
fun LayerItem(
    layer: Layer,
    modifier: Modifier,
    onDelete: (Layer) -> Unit,
    onReset: (Layer) -> Unit,
    onBeatAdjustment: (Layer) -> Unit,
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
                    //마디 조정
                    DropdownMenuItem(
                        text = { Text("마디 조정") },
                        onClick = {
                            menuExpanded = false
                            onBeatAdjustment(layer)
                        }
                    )
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