package com.whistlehub.playlist.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.RemoveCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.whistlehub.R
import com.whistlehub.common.util.LogoutManager
import com.whistlehub.common.util.uriToMultipartBodyPart
import com.whistlehub.common.view.component.CommonAppBar
import com.whistlehub.common.view.component.CustomAlertDialog
import com.whistlehub.common.view.signup.LabeledInputField
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.playlist.viewmodel.PlaylistViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistEditScreen(
    paddingValues: PaddingValues,
    playlistId: Int,
    navController: NavHostController,
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
    logoutManager: LogoutManager
) {
    val coroutineScope = rememberCoroutineScope()
    val customColors = CustomColors()
    val context = LocalContext.current

    // States
    val playlistInfo by playlistViewModel.playlistInfo.collectAsState()
    val playlistTrack by playlistViewModel.playlistTrack.collectAsState()
    val isLoading by playlistViewModel.isLoading.collectAsState()

    var playlistTitle by remember { mutableStateOf("") }
    var playlistDescription by remember { mutableStateOf("") }
    var playlistImage by remember { mutableStateOf<Uri?>(null) }
    var trackList by remember { mutableStateOf(emptyList<com.whistlehub.common.data.remote.dto.response.PlaylistResponse.PlaylistTrackResponse>()) }
    // 이미지 선택 런처
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { playlistImage = it }
    }

    // UI 상태 관리
    var isTitleFocused by remember { mutableStateOf(false) }
    var isDescriptionFocused by remember { mutableStateOf(false) }
    var showDiscardChangesDialog by remember { mutableStateOf(false) }
    var showSaveChangesDialog by remember { mutableStateOf(false) }

    // 트랙 재정렬 관련 변수
    var itemHeightPx by remember { mutableFloatStateOf(0f) }
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var targetIndex by remember { mutableStateOf<Int?>(null) }

    // 스크롤 상태
    val scrollState = rememberScrollState()
    val lazyListState = rememberLazyListState()

    // 플레이리스트 데이터 로드
    LaunchedEffect(playlistId) {
        playlistViewModel.getPlaylistInfo(playlistId)
        playlistViewModel.getPlaylistTrack(playlistId)
    }

    // 로컬 상태 업데이트
    LaunchedEffect(playlistInfo, playlistTrack) {
        playlistTitle = playlistInfo?.name ?: ""
        playlistDescription = playlistInfo?.description ?: ""
        trackList = playlistTrack
    }

    // 뒤로가기 버튼 처리
    BackHandler {
        showDiscardChangesDialog = true
    }

    Scaffold(
        topBar = {
            CommonAppBar(
                title = "Playlist",
                navController = navController,
                logoutManager = logoutManager,
                coroutineScope = coroutineScope,
                showBackButton = true,
                showMenuButton = false,
                onBackClick = { showDiscardChangesDialog = true }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            // 플레이리스트 이미지 업로드 UI
            Box(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                // 플레이리스트 이미지
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(customColors.Grey800)
                        .border(2.dp, customColors.Grey500, RoundedCornerShape(8.dp))
                        .clickable { imageLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (playlistImage != null) {
                        AsyncImage(
                            model = playlistImage,
                            contentDescription = "플레이리스트 이미지",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (playlistInfo?.imageUrl != null) {
                        AsyncImage(
                            model = playlistInfo?.imageUrl,
                            contentDescription = "플레이리스트 이미지",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "기본 플레이리스트",
                            tint = customColors.Grey300,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }

                // 카메라 아이콘
                Box(
                    modifier = Modifier
                        .padding(start = 90.dp, top = 90.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(customColors.Grey800)
                        .border(1.dp, customColors.Grey500, CircleShape)
                        .clickable { imageLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "사진 선택",
                        tint = customColors.Grey50,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // 이미지 삭제 버튼
            if (playlistImage != null || playlistInfo?.imageUrl != null) {
                Button(
                    onClick = { playlistImage = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = customColors.Error700
                    ),
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 16.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text("이미지 삭제")
                }
            }

            // 플레이리스트 제목 입력
            LabeledInputField(
                label = "Playlist Name",
                value = playlistTitle,
                onValueChange = { playlistTitle = it },
                placeholder = "플레이리스트 이름을 입력하세요",
                labelStyle = Typography.bodyLarge.copy(color = customColors.Grey50),
                textStyle = Typography.bodyMedium.copy(color = customColors.Grey50),
                placeholderStyle = Typography.bodyMedium.copy(color = customColors.Grey300),
                isFocused = isTitleFocused,
                onFocusChange = { isTitleFocused = it },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                errorMessage = if (playlistTitle.isBlank()) "플레이리스트 이름은 필수입니다" else null
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 플레이리스트 설명 입력
            LabeledInputField(
                label = "Description",
                value = playlistDescription,
                onValueChange = {
                    if (it.length <= 48) {
                        playlistDescription = it
                    }
                },
                placeholder = "플레이리스트 설명을 입력하세요",
                labelStyle = Typography.bodyLarge.copy(color = customColors.Grey50),
                textStyle = Typography.bodyMedium.copy(color = customColors.Grey50),
                placeholderStyle = Typography.bodyMedium.copy(color = customColors.Grey300),
                isFocused = isDescriptionFocused,
                onFocusChange = { isDescriptionFocused = it },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default), // 줄바꿈 허용
                errorMessage = null,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 트랙 목록 헤더
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "트랙 목록",
                    style = Typography.titleMedium,
                    color = customColors.Grey50
                )

                Text(
                    text = "${trackList.size}개의 트랙",
                    style = Typography.bodyMedium,
                    color = customColors.Grey300
                )
            }

            // 트랙 목록
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                trackList.forEachIndexed { index, track ->
                    val isCurrentDragging = index == draggedIndex
                    var dragOffsetY by remember { mutableFloatStateOf(0f) }
                    val animatedOffsetY by animateFloatAsState(
                        targetValue = dragOffsetY,
                        label = "dragY"
                    )

                    // Show divider at target position during drag
                    if (index == targetIndex) {
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp),
                            color = customColors.Mint500,
                            thickness = 2.dp
                        )
                    }

                    // Track item
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(
                                if (isCurrentDragging) customColors.Grey800.copy(alpha = 0.7f)
                                else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .zIndex(if (isCurrentDragging) 1f else 0f)
                            .offset { IntOffset(0, animatedOffsetY.roundToInt()) }
                            .onGloballyPositioned { coordinates ->
                                itemHeightPx = coordinates.size.height.toFloat()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Delete button
                            Box(
                                modifier = Modifier
                                    .size(25.dp)
                                    .clip(CircleShape)
                                    .background(customColors.Error700)
                                    .clickable {
                                        // Remove track at this index
                                        val newList = trackList.toMutableList()
                                        newList.removeAt(index)
                                        trackList = newList
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Remove,
                                    contentDescription = "Delete Track",
                                    tint = customColors.CommonTextColor,
                                    modifier = Modifier.size(25.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Track image
                            AsyncImage(
                                model = track.trackInfo.imageUrl,
                                contentDescription = "Track Image",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                error = painterResource(id = R.drawable.default_track),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // Track info
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = track.trackInfo.title,
                                    style = Typography.bodyLarge,
                                    color = customColors.Grey50,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = track.trackInfo.nickname,
                                    style = Typography.bodySmall,
                                    color = customColors.Grey300,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Drag handle
                            Icon(
                                imageVector = Icons.Rounded.DragHandle,
                                contentDescription = "Reorder",
                                tint = customColors.Grey400,
                                modifier = Modifier
                                    .size(24.dp)
                                    .pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragStart = {
                                                draggedIndex = index
                                            },
                                            onDragEnd = {
                                                draggedIndex?.let { fromIndex ->
                                                    targetIndex?.let { toIndex ->
                                                        if (fromIndex != toIndex) {
                                                            // Reorder the list
                                                            val newList = trackList.toMutableList()
                                                            val item = newList.removeAt(fromIndex)
                                                            newList.add(toIndex, item)
                                                            trackList = newList
                                                        }
                                                    }
                                                }
                                                draggedIndex = null
                                                dragOffsetY = 0f
                                                targetIndex = null
                                            },
                                            onDragCancel = {
                                                draggedIndex = null
                                                dragOffsetY = 0f
                                                targetIndex = null
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                dragOffsetY += dragAmount.y

                                                // Calculate target index based on drag distance
                                                val movedItems = (dragOffsetY / itemHeightPx).toInt()
                                                targetIndex = (index + movedItems).coerceIn(0, trackList.size - 1)
                                            }
                                        )
                                    }
                            )
                        }
                    }

                    // Show divider at bottom if this is the target and it's the last item
                    if (targetIndex == trackList.size && index == trackList.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp),
                            color = customColors.Mint500,
                            thickness = 2.dp
                        )
                    }
                }
            }

            // 저장 버튼
            Button(
                onClick = { showSaveChangesDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = customColors.CommonButtonColor
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = playlistTitle.isNotBlank()
            ) {
                Text(
                    text = "변경사항 저장",
                    style = Typography.titleMedium,
                    color = customColors.CommonTextColor,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Bottom padding
            Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding()))
        }
    }

    // 전체화면 로딩 인디케이터
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(customColors.Grey900.copy(alpha = 0.8f))
                .zIndex(10f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    color = customColors.Mint500,
                    strokeWidth = 6.dp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "저장 중...",
                    style = Typography.titleMedium,
                    color = customColors.Grey50
                )
            }
        }
    }

    // Discard changes confirmation dialog
    CustomAlertDialog(
        showDialog = showDiscardChangesDialog,
        title = "변경사항 취소",
        message = "변경사항을 취소하고 이전 화면으로 돌아가시겠습니까?",
        confirmButtonText = "취소하기",
        onDismiss = { showDiscardChangesDialog = false },
        onConfirm = { navController.popBackStack() }
    )

    // Save changes confirmation dialog
    CustomAlertDialog(
        showDialog = showSaveChangesDialog,
        title = "변경사항 저장",
        message = "변경사항을 저장하시겠습니까?",
        confirmButtonText = "저장하기",
        onDismiss = { showSaveChangesDialog = false },
        onConfirm = {
            coroutineScope.launch {
                // Prepare image if changed
                val image = if (playlistImage != null) {
                    uriToMultipartBodyPart(context, playlistImage!!)
                } else {
                    null
                }

                // Save changes to playlist
                playlistViewModel.updatePlaylist(
                    playlistId = playlistId,
                    name = playlistTitle,
                    description = playlistDescription,
                    trackIds = trackList.map { it.trackInfo.trackId },
                    image = image
                )

                // Navigate back
                navController.popBackStack()
            }
        }
    )
}