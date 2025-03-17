package com.whistlehub.playlist.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
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
            .fillMaxWidth()
            .height(50.dp)
            .padding(8.dp)
            .clickable {
                // 화면전환
            }
            .background(Color.DarkGray)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentTrack?.title ?: "No Track Playing",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = currentTrack?.artist ?: "Unknown Artist",
                color = Color.LightGray,
                fontSize = 14.sp
            )
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
    }
}
