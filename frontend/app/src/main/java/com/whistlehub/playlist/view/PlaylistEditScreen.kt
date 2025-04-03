package com.whistlehub.playlist.view

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.whistlehub.common.data.remote.dto.response.PlaylistResponse
import com.whistlehub.common.util.uriToMultipartBodyPart
import com.whistlehub.common.view.copmonent.ImageUpload
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.playlist.viewmodel.PlaylistViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PlaylistEditScreen(
    paddingValues: PaddingValues,
    playlistId: Int,
    navController: NavHostController,
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
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showDismissDialog by remember { mutableStateOf(false) }
    var showImageDialog by remember { mutableStateOf(false) }

    // 플레이리스트 정보 수정 관련
    var isEditTitle by remember { mutableStateOf(false) } // 제목 수정 모드
    var isEditDescription by remember { mutableStateOf(false) } // 설명 수정 모드
    // playlistInfo를 구독하여 제목과 설명 자동 업데이트
    var playlistTitle by remember { mutableStateOf("") }
    var playlistDescription by remember { mutableStateOf("") }
    var playlistImage: Uri? by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(playlistInfo) {
        if (!isEditTitle) playlistTitle = playlistInfo?.name ?: ""
        if (!isEditDescription) playlistDescription = playlistInfo?.description ?: ""
        if (playlistInfo?.imageUrl != null) playlistImage = playlistInfo?.imageUrl?.toUri()
    }

    // 트랙 순서 변경 관련
    var itemHeightPx by remember { mutableFloatStateOf(0f) } // 아이템 높이(픽셀 단위)
    var trackList by remember { mutableStateOf(emptyList<PlaylistResponse.PlaylistTrackResponse>()) }
    var draggedVerticalIndex by remember { mutableStateOf<Int?>(null) }  // 트랙 순서 변경 시작 위치
    var targetIndex by remember { mutableStateOf<Int?>(null) } // 드롭 예상 위치

    // 트랙 삭제 관련
    var draggedHorizontalIndex by remember { mutableStateOf<Int?>(null) } // 삭제하려는 index

    // 트랙 리스트가 변경될 때마다 로컬 상태 업데이트
    LaunchedEffect(playlistTrack) {
        trackList = playlistTrack
    }

    BackHandler {
        // 뒤로가기 버튼 클릭 시 수정 취소 확인
        showDismissDialog = true
    }

    LazyColumn(Modifier
        .padding(10.dp)
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = {
                    draggedHorizontalIndex = null
                    draggedVerticalIndex = null
                }
            )
        },
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 플레이리스트 정보 수정
        item {
            Row(Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AsyncImage(
                    model = playlistImage,
                    contentDescription = "Playlist Image",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .clickable {
                            showImageDialog = true
                        },
                    error = null,
                    contentScale = ContentScale.Crop
                )
                Column {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(15.dp)
                    ) { if (isEditTitle) {
                        TextField(
                            value = playlistTitle,
                            onValueChange = { playlistTitle = it },
                            placeholder = { Text("Playlist Name") },
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            singleLine = true
                        )
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = "Check",
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    isEditTitle = false
                                },
                            tint = CustomColors().Grey50
                        )
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Close",
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    isEditTitle = false
                                    playlistTitle = playlistInfo?.name ?: ""
                                },
                            tint = CustomColors().Grey50
                        )
                    } else {
                        Text(
                            playlistTitle,
                            style = Typography.titleLarge,
                            fontSize = Typography.displaySmall.fontSize
                        )
                        Icon(
                            Icons.Rounded.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    isEditTitle = true
                                    isEditDescription = false
                                },
                            tint = CustomColors().Grey50
                        )
                        if (!isEditDescription) {
                            Row(
                                Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(15.dp, Alignment.End),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.Close,
                                    contentDescription = "Close",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable {
                                            // 편집 취소
                                            showDismissDialog = true
                                        })
                                Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = "Check",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable {
                                            coroutineScope.launch {
                                                // 편집 완료
                                                showConfirmDialog = true
                                            }
                                        })
                            }
                        }
                    } }
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isEditDescription) {
                            TextField(
                                value = playlistDescription,
                                onValueChange = { playlistDescription = it },
                                placeholder = { Text("Playlist Description") },
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                singleLine = true
                            )
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = "Check",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable {
                                        isEditDescription = false
                                    },
                                tint = CustomColors().Grey50
                            )
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "Close",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable {
                                        isEditDescription = false
                                        playlistDescription = playlistInfo?.description ?: ""
                                    },
                                tint = CustomColors().Grey50
                            )
                        } else {
                            Text(
                                playlistDescription,
                                style = Typography.bodyMedium,
                                color = CustomColors().Grey200
                            )
                            Icon(
                                Icons.Rounded.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable {
                                        isEditDescription = true
                                        isEditTitle = false
                                    },
                                tint = CustomColors().Grey50
                            )
                        }
                    }
                }
            }
        }

        // 플레이리스트 트랙 순서 수정, 트랙 삭제
        itemsIndexed(trackList) { index, track ->
            val isCurrentVerticalDragging = index == draggedVerticalIndex
            var dragOffsetX by remember { mutableFloatStateOf(0f) }
            var dragOffsetY by remember { mutableFloatStateOf(0f) }
            val animatedOffsetY by animateFloatAsState(
                targetValue = dragOffsetY,
                label = "dragY"
            )
            var showDeleteButton by remember { mutableStateOf(false) }

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
                            isCurrentVerticalDragging -> CustomColors().Grey400.copy(alpha = 0.3f)
                            else -> Color.Transparent
                        },
                        RoundedCornerShape(8.dp)
                    )
                    .zIndex(if (isCurrentVerticalDragging) 1f else 0f)
                    .offset { IntOffset( 0, animatedOffsetY.roundToInt()) }
                    // 아이템 높이 측정 추가
                    .onGloballyPositioned { coordinates ->
                        itemHeightPx = coordinates.size.height.toFloat()
                    }
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                // 드래그 시작 시 드래그 인덱스 설정
                                if (draggedHorizontalIndex != index) {
                                    dragOffsetX = 0f
                                    draggedHorizontalIndex = index
                                }
                                dragOffsetY = 0f
                            },
                            onDragEnd = {
                                if (dragOffsetX < -100) {
                                    showDeleteButton = true
                                    dragOffsetX = -70f
                                } else {
                                    dragOffsetX = 0f
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                dragOffsetX += dragAmount

                                if (dragOffsetX < -100) {
                                    showDeleteButton = true
                                } else {
                                    showDeleteButton = false
                                }
                            }
                        )
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Menu,
                        contentDescription = "Drag Handle",
                        tint = CustomColors().Grey200,
                        modifier = Modifier
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = {
                                        draggedHorizontalIndex = null
                                        draggedVerticalIndex = index
                                    },
                                    onDragEnd = {
                                        draggedVerticalIndex?.let { fromIndex ->
                                            targetIndex?.let { toIndex ->
                                                if (fromIndex != toIndex) {
                                                    trackList = trackList.toMutableList().apply {
                                                        add(toIndex, removeAt(fromIndex))
                                                    }
                                                }
                                            }
                                        }
                                        draggedVerticalIndex = null
                                        dragOffsetY = 0f
                                        targetIndex = null
                                    },
                                    onDrag = { _, dragAmount ->
                                        dragOffsetY += dragAmount.y

                                        val movedItems = (dragOffsetY / itemHeightPx).toInt()
                                        targetIndex = (index + movedItems).coerceIn(0, trackList.size - 1)
                                    }
                                )
                            }
                    )
                    Row(
                        Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp),
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
                    }
                    if ( draggedHorizontalIndex == index ) {
                        Box(Modifier
                            .background(Color.Red)
                            .heightIn(min = 50.dp)
                            .widthIn(max = 70.dp)
                            .width(
                                if (!showDeleteButton && dragOffsetX < 0) {
                                    -dragOffsetX.dp
                                } else if (showDeleteButton && dragOffsetX >= 0) {
                                    dragOffsetX.dp
                                } else if (showDeleteButton && dragOffsetX < 0) {
                                    70.dp
                                } else {
                                    0.dp
                                }
                            )
                            .clickable {
                                if (showDeleteButton) {
                                    // 리스트에서 삭제
                                    trackList = trackList.toMutableList().apply {
                                        removeAt(index)
                                        draggedHorizontalIndex = null
                                    }
                                }
                                showDeleteButton = false
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            if (showDeleteButton) {
                                Text("삭제",
                                    style = Typography.titleMedium,
                                    color = CustomColors().Grey50,
                                )
                            }
                        }
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
        item {
            Spacer(Modifier.height(paddingValues.calculateBottomPadding()))
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("플레이리스트 수정") },
            text = { Text("수정사항을 적용하시겠습니까?") },
            confirmButton = {
                Button({
                    showConfirmDialog = false
                    coroutineScope.launch {
                        val image = if (playlistImage != null && playlistImage != playlistInfo?.imageUrl?.toUri()) {
                            uriToMultipartBodyPart(context, playlistImage!!)
                        } else {
                            null
                        }
                        playlistViewModel.updatePlaylist(
                            playlistId = playlistId,
                            name = playlistTitle,
                            description = playlistDescription,
                            trackIds = trackList.map { it.trackInfo.trackId },
                            image = image
                        )
                        navController.popBackStack()
                    } },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CustomColors().Mint500,
                        contentColor = CustomColors().Grey950
                    )
                    ) {
                    Text("확인")
                }
            },
            dismissButton = {
                Button({
                    showConfirmDialog = false
                },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CustomColors().Grey400,
                        contentColor = CustomColors().Grey950
                    )
                    ) {
                    Text("취소")
                }
            }
        )
    }
    if (showDismissDialog) {
        AlertDialog(
            onDismissRequest = { showDismissDialog = false },
            title = { Text("플레이리스트 수정 취소") },
            text = { Text("모든 수정 사항이 삭제되고, 이전 상태로 돌아갑니다.") },
            confirmButton = {
                Button({
                    showDismissDialog = false
                    navController.popBackStack()
                },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CustomColors().Mint500,
                        contentColor = CustomColors().Grey950
                    )
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                Button({
                    showDismissDialog = false
                },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CustomColors().Grey400,
                        contentColor = CustomColors().Grey950
                    )
                ) {
                    Text("취소")
                }
            }
        )
    }
    if (showImageDialog) {
        AlertDialog(
            onDismissRequest = {
                playlistImage = playlistInfo?.imageUrl?.toUri()
                showImageDialog = false
            },
            title = { Text("플레이리스트 이미지 수정") },
            text = { ImageUpload(
                onChangeImage = { uri ->
                    playlistImage = uri
                },
                originImageUri = playlistInfo?.imageUrl?.toUri(),
                canDelete = false
            ) },
            confirmButton = {
                Button({
                    showImageDialog = false
                },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CustomColors().Mint500,
                        contentColor = CustomColors().Grey950
                    )
                ) {
                    Text("적용")
                }
            },
            dismissButton = {
                Button({
                    playlistImage = playlistInfo?.imageUrl?.toUri()
                    showImageDialog = false
                },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CustomColors().Grey400,
                        contentColor = CustomColors().Grey950
                    )
                ) {
                    Text("취소")
                }
            }
        )
    }
}


