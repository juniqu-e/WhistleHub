package com.whistlehub.playlist.view

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.whistlehub.R
import com.whistlehub.common.view.navigation.Screen
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.playlist.viewmodel.TrackPlayViewModel
import kotlinx.coroutines.launch

@Composable
fun MiniPlayerBar(
    navController: NavController,
    trackPlayViewModel: TrackPlayViewModel = hiltViewModel(),
) {
    // LiveData를 State로 변환하기
    val currentTrack by trackPlayViewModel.currentTrack.collectAsState(initial = null)
    val isPlaying by trackPlayViewModel.isPlaying.collectAsState(initial = false)

    // 위로 드래그 시 전체 트랙 플레이어 화면으로 이동하는 애니메이션 변수
    var dragOffset by remember { mutableStateOf(0f) } // 드래그 거리 저장
    val animatedHeight by animateDpAsState(
        targetValue = (80 + dragOffset / 2).dp, // 드래그 거리에 따라 크기 조절
        animationSpec = tween(durationMillis = 300),
        label = "HeightAnimation"
    )

    // 옆으로 드래그 시 트랙 정지
    var offsetX by remember { mutableStateOf(0f) } // X축 이동 거리
    var dismissed by remember { mutableStateOf(false) } // 사라짐 여부
    val animatedOffsetX by animateFloatAsState(
        targetValue = if (dismissed) 500f else offsetX, // 사라지면 화면 밖으로 이동
        animationSpec = tween(durationMillis = 300),
        label = "OffsetAnimation"
    )
    val alpha by animateFloatAsState(
        targetValue = if (dismissed) 0f else 1f, // 사라질 때 투명도 조절
        animationSpec = tween(durationMillis = 300),
        label = "AlphaAnimation"
    )

    val coroutineScope = rememberCoroutineScope()
    // 미니 플레이어 바 클릭 시 전체 트랙 플레이어 화면으로 이동
    Column(
        modifier = Modifier
            .offset { IntOffset(animatedOffsetX.toInt(), 0) } // X축 이동
            .alpha(alpha) // 투명도 조절
            .height(animatedHeight)  // 드래그 시 높이 조절
            .padding(start = 10.dp, end = 10.dp, bottom = 5.dp)
            .fillMaxWidth()
            .background(
                CustomColors().CommonSubBackgroundColor.copy(alpha = 0.95f),
                shape = RoundedCornerShape(15.dp)
            )
            .pointerInput(Unit) {
                // 상향 드래그 제스처 감지
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (dragOffset > 100) {
                            // 드래그가 끝나면 전체 트랙 플레이어 화면으로 이동
                            navController.navigate(Screen.Player.route)
                        } else {
                            // 드래그가 끝나면 원래 위치로 돌아감
                            dragOffset = 0f
                        }
                    },
                    onVerticalDrag = { _, dragAmount ->
                        dragOffset = (dragOffset - dragAmount).coerceAtLeast(0f) // 위로만 드래그 가능
                    }
                )
            }
            .pointerInput(Unit) {
                // 좌우 드래그 제스처 감지
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX > 250 || offsetX < -250) { // 일정 거리 이상 밀면 사라지기
                            coroutineScope.launch {
                                dismissed = true
                                trackPlayViewModel.stopTrack() // 트랙 정지
                            }
                        } else {
                            offsetX = 0f // 원래 위치로 돌아가기
                        }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        offsetX += dragAmount // 좌우 이동 반영
                    }
                )
            }
            .clickable {
                // 클릭 시 전체 트랙 플레이어 화면으로 이동
                navController.navigate(Screen.Player.route)
            },
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 현재 재생 중인 트랙의 이미지
            if (currentTrack?.imageUrl != null) {
                AsyncImage(
                    model = currentTrack!!.imageUrl,
                    contentDescription = "Track Image",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    placeholder = null,
                    error = painterResource(R.drawable.default_track),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.default_track),
                    contentDescription = "Track Image",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                Text(
                    text = currentTrack?.title ?: "No Track Playing",
                    style = Typography.titleMedium,
                    color = CustomColors().CommonTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = currentTrack?.artist?.nickname ?: "Unknown Artist",
                    style = Typography.bodyMedium,
                    color = CustomColors().CommonSubTextColor,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                IconButton(onClick = {
                    coroutineScope.launch {
                        trackPlayViewModel.previousTrack()
                    }
                }) {
                    Icon(
                        imageVector = Icons.Rounded.FastRewind,
                        contentDescription = "PlayBack",
                        tint = CustomColors().CommonIconColor
                    )
                }
                IconButton(
                    onClick = {
                        if (isPlaying) {
                            trackPlayViewModel.pauseTrack()
                        } else {
                            if (currentTrack == null && trackPlayViewModel.playerTrackList.value.isNotEmpty()) {
                                coroutineScope.launch {
                                    // 트랙이 없을 경우 첫 번째 트랙 재생
                                    trackPlayViewModel.playTrack(trackPlayViewModel.playerTrackList.value[0].trackId)
                                }
                            } else if (currentTrack != null) {
                                trackPlayViewModel.resumeTrack()
                            }
                        }
                    }) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = CustomColors().CommonIconColor
                    )
                }
                IconButton(onClick = {
                    coroutineScope.launch {
                        trackPlayViewModel.nextTrack()
                    }
                }) {
                    Icon(
                        imageVector = Icons.Rounded.FastForward,
                        contentDescription = "PlayForward",
                        tint = CustomColors().CommonIconColor
                    )
                }
            }
        }
    }
}
