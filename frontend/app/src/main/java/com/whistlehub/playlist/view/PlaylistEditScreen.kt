package com.whistlehub.playlist.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.whistlehub.common.data.remote.dto.response.PlaylistResponse
import com.whistlehub.common.view.navigation.Screen
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.playlist.viewmodel.PlaylistViewModel
import kotlin.math.roundToInt

@Composable
fun PlaylistEditScreen(
    playlistId: Int,
    navController: androidx.navigation.NavHostController,
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
) {
    LaunchedEffect(playlistId) {
        // 플레이리스트 트랙 목록을 가져옴
        playlistViewModel.getPlaylistTrack(playlistId)
        // 플레이리스트 정보를 가져옴
        playlistViewModel.getPlaylistInfo(playlistId)
    }

    val playlistTrack by playlistViewModel.playlistTrack.collectAsState()
    val playlistInfo by playlistViewModel.playlistInfo.collectAsState()

    // 로컬 상태 관리
    // 추가할 상태 변수 (Composable 최상단에 선언)
    var itemHeightPx by remember { mutableFloatStateOf(0f) } // 아이템 높이(픽셀 단위)
    var trackList by remember { mutableStateOf(emptyList<PlaylistResponse.PlaylistTrackResponse>()) }
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var targetIndex by remember { mutableStateOf<Int?>(null) } // 드롭 예상 위치

    // 트랙 리스트가 변경될 때마다 로컬 상태 업데이트
    LaunchedEffect(playlistTrack) {
        trackList = playlistTrack
    }
        LazyColumn(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // 플레이리스트 정보 수정
            item {
                Row(Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AsyncImage(
                        model = playlistInfo?.imageUrl,
                        contentDescription = "Playlist Image",
                        modifier = Modifier.size(50.dp).clip(RoundedCornerShape(5.dp)),
                        error = null,
                        contentScale = ContentScale.Crop
                    )
                    Column {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(15.dp)
                        ) {
                            Text(
                                playlistInfo?.name ?: "Playlist Name",
                                style = Typography.titleLarge,
                                fontSize = Typography.displaySmall.fontSize
                            )
                            Icon(
                                Icons.Rounded.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(24.dp).clickable {
                                        navController.navigate(Screen.PlayListEdit.route + "/$playlistId")
                                    },
                                tint = CustomColors().Grey50
                            )
                            Row(
                                Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(15.dp, Alignment.End),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.Close,
                                    contentDescription = "Close",
                                    modifier = Modifier.size(24.dp)
                                        .clickable { navController.popBackStack() })
                                Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = "Check",
                                    modifier = Modifier.size(24.dp).clickable {
                                            navController.popBackStack()
                                        })
                            }
                        }
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                playlistInfo?.description ?: "",
                                style = Typography.bodyLarge,
                                color = CustomColors().Grey200
                            )
                            Icon(
                                Icons.Rounded.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(16.dp).clickable { },
                                tint = CustomColors().Grey50
                            )
                        }
                    }
                }
            }

            // 플레이리스트 트랙 순서 수정
            itemsIndexed(trackList) { index, track ->
                val isCurrentDragging = index == draggedIndex
                val animatedOffsetY by animateFloatAsState(
                    targetValue = if (isCurrentDragging) dragOffsetY else 0f
                )
                if (index == targetIndex) {
                    // 예상 위치에 Divider 삽입
                    HorizontalDivider(modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(CustomColors().Mint500),
                        thickness = 2.dp
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(
                            when {
                                isCurrentDragging -> CustomColors().Grey400.copy(alpha = 0.3f)
                                else -> Color.Transparent
                            },
                            RoundedCornerShape(8.dp)
                        )
                        .zIndex(if (isCurrentDragging) 1f else 0f)
                        .offset { IntOffset(0, animatedOffsetY.roundToInt()) }
                        // 아이템 높이 측정 추가
                        .onGloballyPositioned { coordinates ->
                            itemHeightPx = coordinates.size.height.toFloat()
                        }
                        .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                draggedIndex = index
                            },
                            onDragEnd = {
                                draggedIndex?.let { fromIndex ->
                                    targetIndex?.let { toIndex ->
                                        if (fromIndex != toIndex) {
                                            trackList = trackList.toMutableList().apply {
                                                add(toIndex, removeAt(fromIndex))
                                            }
                                            playlistViewModel.moveTrack(fromIndex, toIndex)
                                        }
                                    }
                                }
                                draggedIndex = null
                                dragOffsetY = 0f
                                targetIndex = null
                            },
                            onDrag = { _, dragAmount ->
                                dragOffsetY += dragAmount.y

                                // 예상 위치 계산 (아이템 높이를 기준으로)
                                val movedItems = (dragOffsetY / itemHeightPx).toInt()
                                targetIndex = (index + movedItems).coerceIn(0, trackList.size - 1)
                            }
                        )
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AsyncImage(
                                model = track.trackInfo.imageUrl,
                                contentDescription = "Track Image",
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                error = null,
                                contentScale = ContentScale.Crop
                            )
                            Column(
                                Modifier
                                    .weight(1f)
                                    .padding(horizontal = 10.dp)
                            ) {
                                Text(
                                    track.trackInfo.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = Typography.titleLarge,
                                    color = CustomColors().Grey50
                                )
                                Text(
                                    track.trackInfo.nickname,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = Typography.bodyMedium,
                                    color = CustomColors().Grey200
                                )
                            }
                        Icon(
                            imageVector = Icons.Rounded.Menu,
                            contentDescription = "Drag",
                            tint = CustomColors().Grey200
                        )
                    }
                }
            }
            // 마지막 아이템 뒤에도 Divider를 추가할 수 있음 (필요 시)
            if (targetIndex == trackList.size) {
                HorizontalDivider(modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(CustomColors().Mint500),
                    thickness = 2.dp
                )
            }
        }
    }
}


