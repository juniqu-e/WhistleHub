package com.whistlehub.playlist.view

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.playlist.viewmodel.PlaylistViewModel
import com.whistlehub.playlist.viewmodel.TrackPlayViewModel
import kotlinx.coroutines.launch

@Composable
fun PlaylistTrackListScreen(
    playlistId: Int,
    navController: NavHostController,
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
    trackPlayViewModel: TrackPlayViewModel = hiltViewModel()
) {
    LaunchedEffect(playlistId) {
        Log.d("PlaylistTrackListScreen", "playlistId: $playlistId")
        // 플레이리스트 트랙 목록을 가져옴
        playlistViewModel.getPlaylistTrack(playlistId)
        // 플레이리스트 정보를 가져옴
        playlistViewModel.getPlaylistInfo(playlistId)
    }
    val coroutineScope = rememberCoroutineScope()
    var showPlayPlaylistDialog by remember { mutableStateOf(false) }
    var showDeletePlaylistDialog by remember { mutableStateOf(false) }
    val playlistTrack by playlistViewModel.playlistTrack.collectAsState()
    val playlistInfo by playlistViewModel.playlistInfo.collectAsState()

    val currentTrack by trackPlayViewModel.currentTrack.collectAsState(initial = null)
    val isPlaying by trackPlayViewModel.isPlaying.collectAsState(initial = false)

    LazyColumn(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Text(
                    playlistInfo?.name ?: "Playlist Name",
                    style = Typography.titleLarge,
                    fontSize = Typography.displaySmall.fontSize,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Rounded.PlayArrow,
                    contentDescription = "Play",
                    modifier = Modifier
                        .size(30.dp)
                        .clickable {
                            showPlayPlaylistDialog = true
                        },
                    tint = CustomColors().Mint500
                )
                Icon(
                    Icons.Rounded.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {},
                    tint = CustomColors().Grey50
                )
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            showDeletePlaylistDialog = true
                        },
                    tint = CustomColors().Grey50
                )
            }
        }
        items(playlistTrack.size) { index ->
            val track = playlistTrack[index]
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
                if (currentTrack?.trackId == track.trackInfo.trackId && isPlaying) {
                // Add current track specific UI here
                IconButton({trackPlayViewModel.pauseTrack()}) {
                    Icon(Icons.Filled.Pause, contentDescription = "Pause", tint = CustomColors().Mint500)
                    }
                } else {
                    IconButton({
                        if (currentTrack?.trackId == track.trackInfo.trackId) {
                            trackPlayViewModel.resumeTrack()
                        } else {
                            coroutineScope.launch {
                                val convertedTrack = trackPlayViewModel.getTrackbyTrackId(track.trackInfo.trackId)
                                if (convertedTrack != null) {
                                    trackPlayViewModel.playTrack(convertedTrack)
                                } else {
                                    Log.d("PlaylistTrackListScreen", "Track not found: ${track.trackInfo.trackId}")
                                }
                            }
                        }
                    }) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = "Play",
                            tint = CustomColors().Grey50
                        )
                    }
                }
                IconButton({}) {
                    Icon(
                        Icons.Rounded.MoreVert,
                        contentDescription = "More Options",
                        tint = CustomColors().Grey50
                    )
                }
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
                Button (
                    onClick = {
                        coroutineScope.launch {
                            playlistViewModel.deletePlaylist(playlistId)
                            navController.popBackStack()
                        }
                        showDeletePlaylistDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CustomColors().Error700,
                        contentColor = CustomColors().Grey950
                    )
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                Button(onClick = { showDeletePlaylistDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CustomColors().Grey400,
                        contentColor = CustomColors().Grey950
                    )) {
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
                                trackPlayViewModel.getTrackbyTrackId(track.trackInfo.trackId)!!
                            }
                            trackPlayViewModel.playPlaylist(convertedTracks)
                        }
                        showPlayPlaylistDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CustomColors().Mint500,
                        contentColor = CustomColors().Grey950
                    )
                ) {
                    Text("재생")
                }
            },
            dismissButton = {
                Button(onClick = { showPlayPlaylistDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CustomColors().Grey400,
                        contentColor = CustomColors().Grey950
                    )) {
                    Text("취소")
                }
            }
        )
    }
}