package com.whistlehub.playlist.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            .clickable {
                // 화면전환
                navController.navigate("player")
            }
            .fillMaxWidth()
            .background(CustomColors().Grey700)
            .padding(vertical = 20.dp, horizontal = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(50.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 현재 재생 중인 트랙의 이미지
            if (currentTrack?.imageUrl != null) {
                AsyncImage(
                    model = currentTrack!!.imageUrl,
                    contentDescription = "Track Image",
                    modifier = Modifier
                        .height(50.dp)
                        .width(50.dp),
                    placeholder = null,
                    error = null,
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.default_track),
                    contentDescription = "Track Image",
                    modifier = Modifier
                        .height(50.dp)
                        .width(50.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Column(Modifier.width(200.dp)) {
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
            IconButton(
                onClick = {
                    if (isPlaying) {
                        trackPlayViewModel.pauseTrack()
                    } else {
                        trackPlayViewModel.playTrack(currentTrack!!)
                    }
                }
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Color.White
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(CustomColors().Grey500)
        ) {
            // 현재 재생 중인 트랙의 진행 상태를 나타내는 UI
            // 예: 진행 바, 시간 표시 등
        }
    }
}
