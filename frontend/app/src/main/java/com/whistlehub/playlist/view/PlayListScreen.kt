package com.whistlehub.playlist.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.whistlehub.R
import com.whistlehub.common.util.LogoutManager
import com.whistlehub.common.view.component.CommonAppBar
import com.whistlehub.common.view.component.CustomAlertDialog
import com.whistlehub.common.view.navigation.Screen
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Pretendard
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.playlist.view.component.CreatePlaylistDialog
import com.whistlehub.playlist.viewmodel.PlaylistViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

/**
 * 플레이리스트 화면 컴포넌트
 * 사용자의 플레이리스트 목록을 표시하고 생성, 수정, 삭제 기능을 제공합니다.
 *
 * @param paddingValues 하단 내비게이션 패딩
 * @param logoutManager 로그아웃 관리자
 * @param navController 네비게이션 컨트롤러
 * @param playlistViewModel 플레이리스트 뷰 모델
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayListScreen(
    paddingValues: PaddingValues,
    logoutManager: LogoutManager,
    navController: NavHostController,
    playlistViewModel: PlaylistViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val customColors = CustomColors()

    // 상태 관리
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showDeletePlaylistDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var selectedPlaylistId by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }

    // 드롭다운 메뉴 상태
    var expandedMenuPlaylistId by remember { mutableIntStateOf(-1) }

    // 초기 데이터 로드
    LaunchedEffect(Unit) {
        playlistViewModel.getPlaylists()
    }

    val playlists = playlistViewModel.playlists.collectAsState()
    val buttonTextStyle = Typography.titleMedium.copy(color = customColors.CommonTextColor)

    // 탑 바
    Scaffold(
        topBar = {
            CommonAppBar(
                title = "Playlist",
                navController = navController,
                logoutManager = logoutManager,
                coroutineScope = coroutineScope
            )
        }
    ) { innerPadding ->
        // 플레이리스트 화면
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 좋아하는 트랙 목록
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                // 좋아하는 트랙 클릭 시 트랙 목록 받아옴
                                navController.navigate(Screen.PlayListTrackList.route + "/like")
                            },
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Favorite,
                            contentDescription = "좋아하는 트랙",
                            tint = customColors.Error700,
                            modifier = Modifier.size(50.dp)
                        )
                        Text(
                            "Liked Track",
                            modifier = Modifier.weight(1f),
                            fontSize = Typography.titleLarge.fontSize,
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Bold,
                            color = customColors.CommonTextColor
                        )
                    }
                }

                // 플레이리스트 목록
                items(playlists.value.size) { index ->
                    val playlist = playlists.value[index]
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                            .clickable {
                                // 플레이리스트 클릭 시 트랙 목록 받아옴
                                navController.navigate(Screen.PlayListTrackList.route + "/${playlist.playlistId}")
                            },
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = playlist.imageUrl,
                            contentDescription = "${playlist.name} 이미지",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            error = painterResource(R.drawable.default_track),
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            playlist.name,
                            modifier = Modifier.weight(1f),
                            fontSize = Typography.titleLarge.fontSize,
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Bold,
                            color = customColors.CommonTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // 드롭다운
                        Box {
                            IconButton(onClick = { expandedMenuPlaylistId = playlist.playlistId }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "더 보기",
                                    tint = customColors.CommonIconColor
                                )
                            }

                            DropdownMenu(
                                expanded = expandedMenuPlaylistId == playlist.playlistId,
                                onDismissRequest = { expandedMenuPlaylistId = -1 },
                                modifier = Modifier.background(customColors.Grey800)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("수정", color = customColors.Grey50) },
                                    onClick = {
                                        expandedMenuPlaylistId = -1
                                        navController.navigate(Screen.PlayListEdit.route + "/${playlist.playlistId}")
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = customColors.Grey50
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("삭제", color = customColors.Error700) },
                                    onClick = {
                                        expandedMenuPlaylistId = -1
                                        selectedPlaylistId = playlist.playlistId
                                        showDeletePlaylistDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = customColors.Error700
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                // 하단 여백
                item {
                    Spacer(Modifier.height(80.dp))
                }
            }

            // Create Playlist 버튼 (하단 고정)
            Button(
                onClick = { showCreatePlaylistDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = customColors.CommonButtonColor
                )
            ) {
                Text(
                    "플레이리스트 생성하기",
                    style = buttonTextStyle
                )
            }

            // 바텀 내비게이션 패딩
            Spacer(Modifier.height(paddingValues.calculateBottomPadding()))
        }
    }

    // 성공 알림 다이얼로그
    CustomAlertDialog(
        showDialog = showSuccessDialog,
        title = "플레이리스트 생성 완료",
        message = successMessage,
        onDismiss = { showSuccessDialog = false },
        onConfirm = { showSuccessDialog = false }
    )

    // 플레이리스트 생성 다이얼로그
    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = {
                Text(
                    text = "플레이리스트 생성",
                    style = Typography.titleLarge,
                    color = customColors.CommonTextColor,
                )
            },
            text = {
                CreatePlaylistDialog(
                    onDismiss = { showCreatePlaylistDialog = false },
                    onCreatePlaylist = { title, description, image ->
                        isLoading = true
                        coroutineScope.launch {
                            try {
                                playlistViewModel.createPlaylist(
                                    name = title,
                                    description = description,
                                    image = image
                                )
                                successMessage = "플레이리스트가 성공적으로 생성되었습니다."
                                showCreatePlaylistDialog = false
                                showSuccessDialog = true
                                isLoading = false
                            } catch (e: Exception) {
                                isLoading = false
                                // Handle error
                            }
                        }
                    }
                )
            },
            confirmButton = { /* Empty because buttons are handled in the dialog itself */ },
            dismissButton = { /* Empty because buttons are handled in the dialog itself */ },
            containerColor = customColors.Grey800,
            modifier = Modifier
                .fillMaxWidth(0.95f)  // 95% of screen width
                .padding(8.dp),
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false  // Disable platform default width constraint
            )
        )
    }

    // 플레이리스트 삭제 확인 다이얼로그
    if (showDeletePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showDeletePlaylistDialog = false },
            title = { Text("플레이리스트 삭제") },
            text = { Text("플레이리스트를 삭제하시겠습니까?") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            playlistViewModel.deletePlaylist(selectedPlaylistId)
                        }
                        showDeletePlaylistDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = customColors.Error700,
                        contentColor = customColors.CommonTextColor
                    )
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeletePlaylistDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = customColors.CommonTextColor
                    ),
                    border = BorderStroke(1.dp, customColors.CommonOutLineColor),
                ) {
                    Text("취소")
                }
            }
        )
    }
}