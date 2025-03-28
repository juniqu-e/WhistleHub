package com.whistlehub.playlist.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.whistlehub.common.view.navigation.Screen
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Pretendard
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.playlist.view.component.CreatePlaylist
import com.whistlehub.playlist.viewmodel.PlaylistViewModel

@Composable
fun PlayListScreen(navController: NavHostController, playlistViewModel: PlaylistViewModel = hiltViewModel()) {
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }

    playlistViewModel.getPlaylists()
//    LaunchedEffect(Unit) {
//        // 플레이리스트 목록을 가져옴
//    }
    val playlists = playlistViewModel.playlists.collectAsState()

    // 플레이리스트 화면
    LazyColumn(Modifier
        .height(800.dp)
        .padding(horizontal = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp))
    {
        // 페이지 제목
        item {
            Text(
                "Playlist",
                modifier = Modifier.fillMaxSize(),
                style = Typography.displaySmall,
                fontFamily = Pretendard,
                color = CustomColors().Grey50,
            )
        }

        // 내가 만든 트랙 목록
        item {
            Row(Modifier
                .fillMaxWidth()
                .clickable {},
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.MusicNote, contentDescription = "내 트랙", tint = CustomColors().Mint500, modifier = Modifier.size(40.dp))
                Text("My Track",
                    modifier = Modifier.weight(1f),
                    fontSize = Typography.titleLarge.fontSize,
                    fontFamily = Pretendard,
                    color = CustomColors().Grey50
                )
            }
        }

        // 좋아하는 트랙 목록
        item {
            Row(Modifier
                .fillMaxWidth()
                .clickable {},
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Favorite, contentDescription = "좋아하는 트랙", tint = CustomColors().Mint500, modifier = Modifier.size(40.dp))
                Text("Liked Track",
                    modifier = Modifier.weight(1f),
                    fontSize = Typography.titleLarge.fontSize,
                    fontFamily = Pretendard,
                    color = CustomColors().Grey50
                )
            }
        }

        // 플레이리스트
        items(playlists.value.size) { index ->
            val playlist = playlists.value[index]
            Row(Modifier
                .fillMaxWidth()
                .clickable {
                    // 플레이리스트 클릭 시 트랙 목록 받아옴
                    navController.navigate(Screen.PlayListTrackList.route + "/${playlist.playlistId}")
                },
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = playlist.imageUrl,
                    contentDescription = "${playlist.name} 이미지",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
                Text(playlist.name,
                    modifier = Modifier.weight(1f),
                    fontSize = Typography.titleLarge.fontSize,
                    fontFamily = Pretendard,
                    color = CustomColors().Grey50,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    Icons.Rounded.Edit,
                    contentDescription = "플레이리스트 수정",
                    tint = CustomColors().Grey50,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {}
                )
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "플레이리스트 삭제",
                    tint = CustomColors().Grey50,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {}
                )
            }
        }

        // 플레이리스트 추가
        item {
            Row(Modifier
                .fillMaxWidth()
                .clickable {
                    showCreatePlaylistDialog = true
                },
                horizontalArrangement = Arrangement.spacedBy(10.dp, alignment = Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AddCircleOutline, contentDescription = "플레이리스트 추가", tint = CustomColors().Grey50, modifier = Modifier.size(24.dp))
                Text("Create Playlist",
                    modifier = Modifier.padding(10.dp),
                    fontSize = Typography.titleLarge.fontSize,
                    fontFamily = Pretendard,
                    color = CustomColors().Grey50
                )
            }
        }
    }

    // CreatePlaylist Dialog
    var playlistTitle by remember { mutableStateOf("") }
    var playlistDescription by remember { mutableStateOf("") }

    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = { Text(
                text = "플레이리스트 생성",
                style = Typography.titleLarge,
                color = CustomColors().Grey50,
            ) },
            text = { CreatePlaylist(
                onInputTitle = { playlistTitle = it},
                onInputDescription = { playlistDescription = it },
            ) },
            modifier = Modifier
                .fillMaxWidth()
                .background(CustomColors().Grey950),
            confirmButton = {
                Button(
                    onClick = {
                        playlistViewModel.createPlaylist(name = playlistTitle, description = playlistDescription)
                        showCreatePlaylistDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CustomColors().Mint500,
                        contentColor = CustomColors().Grey950,
                    )
                ) {
                    Text("생성", style = Typography.bodyLarge)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showCreatePlaylistDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CustomColors().Grey400,
                        contentColor = CustomColors().Grey50,
                    )
                ) {
                    Text("취소", style = Typography.bodyLarge)
                }
            }
        )
    }
}