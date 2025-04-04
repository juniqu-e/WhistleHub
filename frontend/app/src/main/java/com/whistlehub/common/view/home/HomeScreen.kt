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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.whistlehub.common.data.remote.dto.response.TrackResponse
import com.whistlehub.common.util.LogoutManager
import com.whistlehub.common.view.component.CommonAppBar
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.common.view.track.NewTrackCard
import com.whistlehub.common.view.track.TrackListRow
import com.whistlehub.playlist.viewmodel.TrackPlayViewModel

@OptIn(ExperimentalLayoutApi::class)
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun HomeScreen(
    paddingValues: PaddingValues,
    trackPlayViewModel: TrackPlayViewModel = hiltViewModel(),
    navController: NavHostController,
    logoutManager: LogoutManager
) {
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        trackPlayViewModel.getTempTrackList() // 트랙 리스트 가져오기
    }

    // 트랙 리스트 UI
    val trackList by trackPlayViewModel.trackList.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            CommonAppBar(
                title = "Whistle Hub",
                navController = navController,
                logoutManager = logoutManager,
                coroutineScope = coroutineScope
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            // 최근 올라온 트랙
            item {
                Text(
                    text = "최근 올라온 트랙",
                    style = Typography.titleLarge,
                    modifier = Modifier.padding(start = 10.dp, top = 10.dp, end = 10.dp)
                )

                LazyRow(
                    Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(trackList.size) { index ->
                        var track by remember { mutableStateOf<TrackResponse.GetTrackDetailResponse?>(null) }
                        LaunchedEffect(Unit) {
                            track = trackPlayViewModel.getTrackbyTrackId(trackList[index].trackId)
                        }
                        track?.let { track ->
                            // 트랙 카드
                            NewTrackCard(
                                track = track,
                                trackPlayViewModel = trackPlayViewModel,
                                navController = navController
                            )
                        }
                    }
                }
            }

            // 최근 들은 느낌의 음악
            item {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "최근 들은 느낌의 음악",
                            style = Typography.titleLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Text("전체보기", style = Typography.bodyLarge, modifier = Modifier.clickable {
                            // TODO: 더보기 클릭 시 동작
                        }, textAlign = TextAlign.End)
                    }
                    TrackListRow(trackList = trackList)
                }
            }

            // 한번도 안 들어본 음악
            item {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "한번도 안 들어본 음악",
                            style = Typography.titleLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Text("전체보기", style = Typography.bodyLarge, modifier = Modifier.clickable {
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
}