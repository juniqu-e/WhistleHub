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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.whistlehub.R
import com.whistlehub.common.util.LogoutManager
import com.whistlehub.common.view.component.CommonAppBar
import com.whistlehub.common.view.navigation.Screen
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.common.view.track.TrackItemRow
import com.whistlehub.playlist.data.TrackEssential
import com.whistlehub.playlist.viewmodel.PlaylistViewModel
import com.whistlehub.playlist.viewmodel.TrackPlayViewModel
import com.whistlehub.workstation.viewmodel.WorkStationViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow

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

    LaunchedEffect(playlistId) {
        Log.d("PlaylistTrackListScreen", "playlistId: $playlistId")
        if (isLikedPlaylist) {
            // playlistId가 "like"인 경우, 좋아요 트랙 목록을 가져옴
            playlistViewModel.getLikeTracks()
        } else {
            try {
                // playlistId가 "like"가 아닌 경우
                val numericPlaylistId = playlistId.toInt()
                // 플레이리스트 트랙 목록을 가져옴
                playlistViewModel.getPlaylistTrack(numericPlaylistId)
                // 플레이리스트 정보를 가져옴
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
                Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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
                    Divider(
                        Modifier
                            .fillMaxWidth()
                    )
                }
                item {
                    Spacer(Modifier.height(paddingValues.calculateBottomPadding()))
                }
            }
        }

        // 삭제 다이얼로그
        if (showDeletePlaylistDialog) {
            AlertDialog(
                onDismissRequest = { showDeletePlaylistDialog = false },
                title = { Text("플레이리스트 삭제") },
                text = { Text("플레이리스트를 삭제하시겠습니까?") },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                playlistViewModel.deletePlaylist(playlistId.toInt())
                                navController.popBackStack()
                            }
                            showDeletePlaylistDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CustomColors().Error700,
                            contentColor = CustomColors().CommonTextColor
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
                            contentColor = CustomColors().CommonTextColor
                        ),
                        border = BorderStroke(1.dp, CustomColors().CommonOutLineColor),
                    ) {
                        Text("취소")
                    }
                }
            )
        }
        if (showPlayPlaylistDialog) {
            AlertDialog(
                onDismissRequest = { showPlayPlaylistDialog = false },
                title = { Text("플레이리스트 재생") },
                text = { Text("플레이리스트를 재생하시겠습니까?") },
                confirmButton = {
                    Button(
                        onClick = {
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
                            showPlayPlaylistDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CustomColors().CommonButtonColor,
                            contentColor = CustomColors().CommonTextColor
                        )
                    ) {
                        Text("재생")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showPlayPlaylistDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = CustomColors().CommonTextColor
                        ),
                        border = BorderStroke(1.dp, CustomColors().CommonOutLineColor),
                    ) {
                        Text("취소")
                    }
                }
            )
        }
    }
}

@Composable
fun PlaylistHeader(
    isLikedPlaylist: Boolean,
    playlistInfo: com.whistlehub.common.data.remote.dto.response.PlaylistResponse.GetPlaylistResponse?,
    tracksCount: Int,
    totalDuration: String,
    onPlayClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier
) {
    val colors = CustomColors()

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Playlist Image or Liked Icon
        if (isLikedPlaylist) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.CommonBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = "Favorite",
                    modifier = Modifier.size(100.dp),
                    tint = colors.Error700
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Liked Tracks",
                style = Typography.displaySmall,
                color = CustomColors().Grey50
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Your Favorite Tracks",
                style = Typography.bodyMedium,
                color = CustomColors().Grey300,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(16.dp))
        } else {
            AsyncImage(
                model = playlistInfo?.imageUrl,
                contentDescription = "Playlist Cover",
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.default_track)
            )

            Spacer(Modifier.height(16.dp))
            Text(
                playlistInfo?.name ?: "Playlist",
                style = Typography.displaySmall,
                color = CustomColors().Grey50
            )
            Spacer(Modifier.height(8.dp))
            Text(
                playlistInfo?.description ?: "",
                style = Typography.bodyMedium,
                color = CustomColors().Grey300,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(16.dp))
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
        ) {
            // Edit Playlist Button (Now First)
            Button(
                onClick = { onEditClick() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CustomColors().CommonButtonColor,
                ),
                shape = RoundedCornerShape(8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(20.dp),
                        tint = colors.CommonTextColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Edit Playlist",
                        style = Typography.bodyLarge,
                        color = CustomColors().CommonTextColor,
                    )
                }
            }

            // Play All Button (Now Second)
            Button(
                onClick = onPlayClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CustomColors().CommonButtonColor,
                ),
                shape = RoundedCornerShape(8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        modifier = Modifier.size(20.dp),
                        tint = colors.CommonTextColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Play All",
                        style = Typography.bodyLarge,
                        color = CustomColors().CommonTextColor,
                    )
                }
            }
        }
    }
}

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