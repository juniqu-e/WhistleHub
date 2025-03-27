package com.whistlehub.common.view.home

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.whistlehub.common.view.copmonent.NewTrackCard
import com.whistlehub.common.view.copmonent.TrackListRow
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.playlist.viewmodel.TrackPlayViewModel

@OptIn(ExperimentalLayoutApi::class)
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun HomeScreen(paddingValues: PaddingValues) {
    // ViewModel 초기화
    val trackPlayViewModel = hiltViewModel<TrackPlayViewModel>()

    // 트랙 리스트 UI
    val trackList by trackPlayViewModel.trackList.collectAsState(initial = emptyList())

    // 추천태그 리스트
    val tags = listOf("Pop", "Rock", "Jazz", "Classical", "Hip-Hop", "Electronic", "Indie", "R&B", "Country", "Reggae") // 예시 태그 리스트
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(30.dp)
    ) {
        // 로고 영역(로고 누르면 재생 정지/재생바 사라짐)
        item {
            Text(
                text = "WhistleHub",
                style = Typography.titleLarge,
                modifier = Modifier
                    .padding(10.dp)
                    .clickable {
                        trackPlayViewModel.stopTrack()
                    }
            )
        }

        // 최근 올라온 트랙
        item {
            LazyRow(
                Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(trackList.size) { index ->
                    NewTrackCard(trackList[index])
                }
            }
        }

        // 추천 태그
        item {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("추천 태그", style = Typography.titleLarge, modifier = Modifier.weight(1f))
                    Text(
                        "더보기",
                        style = Typography.bodyLarge,
                        modifier = Modifier.clickable {},
                        textAlign = TextAlign.End
                    )
                }
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tags.size) { index ->
                        Button(
                            onClick = { /* TODO: 태그 클릭 시 동작 */ }, modifier = Modifier.padding(4.dp)
                        ) {
                            Text(tags[index], style = Typography.titleMedium)
                        }
                    }
                }
            }
        }

        // 최근 들은 느낌의 음악
        item {
            Column {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("최근 들은 느낌의 음악", style = Typography.titleLarge, modifier = Modifier.weight(1f))
                    Text("전체보기", style = Typography.bodyLarge, modifier = Modifier.clickable{
                        // TODO: 더보기 클릭 시 동작
                    }, textAlign = TextAlign.End)
                }
                TrackListRow(trackList = trackList)
            }
        }

        // 한번도 안 들어본 음악
        item {
            Column {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("한번도 안 들어본 음악", style = Typography.titleLarge, modifier = Modifier.weight(1f))
                    Text("전체보기", style = Typography.bodyLarge, modifier = Modifier.clickable{
                        // TODO: 더보기 클릭 시 동작
                    }, textAlign = TextAlign.End)
                }
                TrackListRow(trackList = trackList)
            }
        }

        // 최하단 Space
        item {
            Spacer(
                Modifier.height(paddingValues.calculateBottomPadding())
            )
        }
    }
}