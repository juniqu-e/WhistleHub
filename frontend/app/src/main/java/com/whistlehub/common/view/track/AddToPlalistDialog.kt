package com.whistlehub.common.view.track

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddBox
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.whistlehub.common.data.remote.dto.response.PlaylistResponse
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.playlist.viewmodel.PlaylistViewModel

@Composable
fun AddToPlaylistDialog(
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
    onPlaylistSelect: (Int) -> Unit = {},
) {
    // Playlist에 추가하는 다이얼로그 UI

    // 플레이리스트 목록 가져오기
    playlistViewModel.getPlaylists() // 최신화
    val myPlaylists by playlistViewModel.playlists.collectAsState()
    var selectedPlaylist by remember { mutableStateOf(PlaylistResponse.GetMemberPlaylistsResponse(
        playlistId = 0,
        name = "플레이리스트",
        imageUrl = null.toString()
    )) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        myPlaylists.forEach { playlist ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedPlaylist = playlist }
                    .background(if (selectedPlaylist == playlist) CustomColors().Grey600 else Color.Transparent, shape = RoundedCornerShape(10.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AsyncImage(
                    model = playlist.imageUrl,
                    contentDescription = "Playlist Image ${playlist.playlistId}",
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Crop,
                )
                Text(text = playlist.name,
                    style = Typography.bodyLarge,
                    color = CustomColors().Grey200,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(Icons.Rounded.AddBox, contentDescription = "Add to Playlist",
                    tint = CustomColors().Grey200,
                    modifier = Modifier
                        .size(20.dp)
                )
            }
        }
        Button({
            onPlaylistSelect(selectedPlaylist.playlistId)
        }, modifier = Modifier.background(ButtonDefaults.buttonColors().containerColor, RoundedCornerShape(10.dp))) {
            Text(
                text = "Add to Playlist",
                style = Typography.bodyLarge,
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                textAlign = TextAlign.Center
            )
        }
        Button({}, modifier = Modifier.border(1.dp, CustomColors().Mint500, RoundedCornerShape(10.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = CustomColors().Mint500
            )) {
            Text(
                text = "Create New Playlist",
                style = Typography.bodyLarge,
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}