package com.whistlehub.playlist.view

import android.util.Log
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.whistlehub.common.util.LogoutManager
import com.whistlehub.common.view.component.CommonAppBar
import com.whistlehub.common.view.navigation.Screen
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.common.view.track.TrackItemRow
import com.whistlehub.playlist.data.TrackEssential
import com.whistlehub.playlist.view.components.DeletePlaylistDialog
import com.whistlehub.playlist.view.components.PlayPlaylistDialog
import com.whistlehub.playlist.view.components.PlaylistHeader
import com.whistlehub.playlist.viewmodel.PlaylistViewModel
import com.whistlehub.playlist.viewmodel.TrackPlayViewModel
import com.whistlehub.workstation.viewmodel.WorkStationViewModel
import kotlinx.coroutines.launch

/**
 * 플레이리스트 트랙 목록 화면 컴포넌트
 *
 * @param paddingValues 하단 내비게이션 패딩
 * @param playlistId 플레이리스트 ID (문자열)
 * @param navController 네비게이션 컨트롤러
 * @param trackPlayViewModel 트랙 재생을 위한 뷰모델
 * @param playlistViewModel 플레이리스트 데이터를 관리하는 뷰모델
 * @param workStationViewModel 워크스테이션 관련 뷰모델
 * @param logoutManager 로그아웃 관리자
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistTrackListScreen(
    paddingValues: PaddingValues,
    playlistId: String,
    navController: NavHostController,
    trackPlayViewModel: TrackPlayViewModel = hiltViewModel(),
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
    workStationViewModel: WorkStationViewModel,
    logoutManager: LogoutManager = hiltViewModel(),
) {
    val coroutineScope = rememberCoroutineScope()
    var showPlayPlaylistDialog by remember { mutableStateOf(false) }
    var showDeletePlaylistDialog by remember { mutableStateOf(false) }
    val playlistTrack by playlistViewModel.playlistTrack.collectAsState()
    val playlistInfo by playlistViewModel.playlistInfo.collectAsState()
    val isLikedPlaylist = playlistId == "like"

    // 데이터 로드
    LaunchedEffect(playlistId) {
        Log.d("PlaylistTrackListScreen", "playlistId: $playlistId")
        if (isLikedPlaylist) {
            // 좋아요 트랙 목록을 가져옴
            playlistViewModel.getLikeTracks()
        } else {
            try {
                val numericPlaylistId = playlistId.toInt()
                // 플레이리스트 트랙 목록과 정보를 가져옴
                playlistViewModel.getPlaylistTrack(numericPlaylistId)
                playlistViewModel.getPlaylistInfo(numericPlaylistId)
            } catch (e: NumberFormatException) {
                Log.e("PlaylistTrackListScreen", "Invalid playlist ID: $playlistId", e)
            }
        }
    }

    Scaffold(
        topBar = {
            CommonAppBar(
                title = "Playlist",
                navController = navController,
                logoutManager = logoutManager,
                coroutineScope = coroutineScope,
                showBackButton = true,
                showMenuButton = true,
            )
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 플레이리스트 헤더
                item {
                    PlaylistHeader(
                        isLikedPlaylist = isLikedPlaylist,
                        playlistInfo = playlistInfo,
                        tracksCount = playlistTrack.size,
                        totalDuration = calculateTotalDuration(playlistTrack),
                        onPlayClick = { showPlayPlaylistDialog = true },
                        onEditClick = {
                            if (!isLikedPlaylist) {
                                navController.navigate(Screen.PlayListEdit.route + "/$playlistId")
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Divider(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    )
                }

                // 트랙 목록이 비어있는 경우 안내 메시지 표시
                if (playlistTrack.isEmpty()) {
                    item {
                        // 빈 플레이리스트 UI 직접 구현
                        Column(
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 64.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // 음악 없음 아이콘
                            Icon(
                                imageVector = Icons.Default.MusicOff,
                                contentDescription = null,
                                tint = CustomColors().Grey500,
                                modifier = Modifier.size(80.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // 주요 메시지
                            Text(
                                text = if (isLikedPlaylist)
                                    "좋아요한 트랙이 없습니다."
                                else
                                    "플레이리스트에 음악이 없습니다.",
                                style = Typography.titleLarge,
                                color = CustomColors().Grey200,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // 설명 메시지
                            Text(
                                text = if (isLikedPlaylist)
                                    "마음에 드는 트랙에 좋아요를 눌러보세요."
                                else
                                    "음악을 추가해서 플레이리스트를 채워보세요.",
                                style = Typography.bodyMedium,
                                color = CustomColors().Grey400,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            // 검색 화면으로 이동하는 버튼
                            Button(
                                onClick = { navController.navigate(Screen.Search.route) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CustomColors().CommonButtonColor
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = CustomColors().CommonTextColor
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "음악 검색하기",
                                        style = Typography.bodyLarge,
                                        color = CustomColors().CommonTextColor
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // 트랙 목록이 있는 경우 트랙 아이템들을 표시
                    items(playlistTrack.size) { index ->
                        val trackData = playlistTrack[index]
                        val track = TrackEssential(
                            trackId = trackData.trackInfo.trackId,
                            title = trackData.trackInfo.title,
                            artist = trackData.trackInfo.nickname,
                            imageUrl = trackData.trackInfo.imageUrl,
                        )
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(Modifier.weight(1f)) {
                                TrackItemRow(
                                    track,
                                    workStationViewModel = workStationViewModel,
                                    navController = navController,
                                )
                            }
                        }
                        Divider(Modifier.fillMaxWidth())
                    }
                }

                // 하단 패딩 공간
                item {
                    Spacer(Modifier.height(paddingValues.calculateBottomPadding()))
                }
            }
        }

        // 플레이리스트 삭제 확인 다이얼로그
        if (showDeletePlaylistDialog) {
            DeletePlaylistDialog(
                onDismiss = { showDeletePlaylistDialog = false },
                onConfirm = {
                    coroutineScope.launch {
                        playlistViewModel.deletePlaylist(playlistId.toInt())
                        navController.popBackStack()
                    }
                }
            )
        }

        // 플레이리스트 재생 확인 다이얼로그
        if (showPlayPlaylistDialog && playlistTrack.isNotEmpty()) {
            PlayPlaylistDialog(
                onDismiss = { showPlayPlaylistDialog = false },
                onConfirm = {
                    coroutineScope.launch {
                        val convertedTracks = playlistTrack.map { track ->
                            TrackEssential(
                                trackId = track.trackInfo.trackId,
                                title = track.trackInfo.title,
                                artist = track.trackInfo.nickname,
                                imageUrl = track.trackInfo.imageUrl,
                            )
                        }
                        trackPlayViewModel.playPlaylist(convertedTracks)
                    }
                }
            )
        }
    }
}

/**
 * 트랙 목록의 총 재생 시간을 계산하여 문자열로 반환
 */
private fun calculateTotalDuration(tracks: List<com.whistlehub.common.data.remote.dto.response.PlaylistResponse.PlaylistTrackResponse>): String {
    val totalSeconds = tracks.sumOf { it.trackInfo.duration.toLong() }
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}