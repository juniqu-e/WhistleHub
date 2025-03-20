package com.whistlehub.common.view.home

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.whistlehub.common.view.copmonent.TrackListColumn
import com.whistlehub.common.view.copmonent.TrackListRow
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.playlist.viewmodel.TrackPlayViewModel

@OptIn(ExperimentalLayoutApi::class)
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun HomeScreen() {
    // ViewModel 초기화
    val trackPlayViewModel: TrackPlayViewModel = hiltViewModel()

    // 트랙 로드
    trackPlayViewModel.loadTracks()

    // 트랙 리스트 UI
    val trackList = trackPlayViewModel.trackList.collectAsState(initial = emptyList())

    Column {
        Text("홈화면", Modifier.fillMaxWidth(), style = Typography.displaySmall, textAlign = TextAlign.Center)
        Button({
            trackPlayViewModel.stopTrack()
        }) {
            Text("정지")
        }
        TrackListColumn(trackList = trackList)
        TrackListRow(trackList = trackList)
    }
}