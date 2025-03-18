package com.whistlehub.common.view.home

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.whistlehub.playlist.viewmodel.TrackPlayViewModel

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun HomeScreen() {
    // ViewModel 초기화
    val trackPlayViewModel: TrackPlayViewModel = hiltViewModel()

    // 트랙 로드
    trackPlayViewModel.loadTracks()

    // 트랙 리스트 UI
    Column {
        Text("트랙 리스트")
        trackPlayViewModel.trackList.value.forEach { track ->
            Button(onClick = {
                // 트랙 재생 버튼 클릭 시 ViewModel을 통해 트랙 재생
                trackPlayViewModel.playTrack(track)
            }) {
                Text("재생: ${track.title}")
            }
        }
        Button({
            trackPlayViewModel.pauseTrack()
        }) {
            Text("일시정지")
        }
        Button({
            trackPlayViewModel.stopTrack()
        }) {
            Text("정지")
        }
    }
}