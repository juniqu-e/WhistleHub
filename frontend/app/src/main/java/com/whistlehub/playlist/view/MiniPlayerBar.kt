package com.whistlehub.playlist.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.whistlehub.R
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.playlist.viewmodel.TrackPlayViewModel

@Composable
fun MiniPlayerBar(
    navController: NavController,
    trackPlayViewModel: TrackPlayViewModel = hiltViewModel(),
) {
    // LiveData를 State로 변환하기
    val currentTrack by trackPlayViewModel.currentTrack.collectAsState(initial = null)
    val isPlaying by trackPlayViewModel.isPlaying.collectAsState(initial = false)

    // 미니 플레이어 바 클릭 시 전체 트랙 플레이어 화면으로 이동
    Column(
        modifier = Modifier
            .padding(start = 10.dp, end = 10.dp, bottom = 5.dp)
            .clickable {
                // 화면전환
                navController.navigate("player")
            }
            .fillMaxWidth()
            .background(
                CustomColors().Grey700.copy(alpha = 0.95f), shape = RoundedCornerShape(15.dp)
            )
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
                    error = null,
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
                    style = Typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = currentTrack?.artist?.nickname ?: "Unknown Album",
                    style = Typography.bodyLarge,
                    color = CustomColors().Grey400,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                IconButton(onClick = {
                    trackPlayViewModel.previousTrack()
                }) {
                    Icon(
                        imageVector = Icons.Rounded.FastRewind,
                        contentDescription = "PlayBack",
                        tint = Color.White
                    )
                }
                IconButton(
                    onClick = {
                        if (isPlaying) {
                            trackPlayViewModel.pauseTrack()
                        } else {
                            if (currentTrack == null && trackPlayViewModel.playerTrackList.value.isNotEmpty()) {
                                // 트랙이 없을 경우 첫 번째 트랙 재생
                                trackPlayViewModel.playTrack(trackPlayViewModel.playerTrackList.value[0])
                            } else if (currentTrack != null) {
                                trackPlayViewModel.resumeTrack()
                            }
                        }
                    }) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White
                    )
                }
                IconButton(onClick = {
                    trackPlayViewModel.nextTrack()
                }) {
                    Icon(
                        imageVector = Icons.Rounded.FastForward,
                        contentDescription = "PlayForward",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
