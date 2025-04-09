package com.whistlehub.search.view

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.whistlehub.common.util.LogoutManager
import com.whistlehub.common.view.component.CommonAppBar
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.common.view.track.TrackItemRow
import com.whistlehub.common.view.track.TrackItemStyle
import com.whistlehub.common.view.track.TrackListRow
import com.whistlehub.playlist.data.TrackEssential
import com.whistlehub.playlist.viewmodel.TrackPlayViewModel
import com.whistlehub.search.viewmodel.SearchViewModel
import com.whistlehub.workstation.viewmodel.WorkStationViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagRankingScreen(
    tagId: Int,
    tagName: String,
    paddingValues: PaddingValues,
    trackPlayViewModel: TrackPlayViewModel = hiltViewModel(),
    searchViewModel: SearchViewModel,
    workStationViewModel: WorkStationViewModel,
    navController: NavHostController,
    logoutManager: LogoutManager
) {
    LaunchedEffect(Unit) {
        searchViewModel.getRankingByTag(tagId, "WEEK")
        searchViewModel.getTagRecommendTrack(tagId)
        searchViewModel.getTagRecentTrack(tagId)
    }
    val coroutineScope = rememberCoroutineScope()
    val tagRanking by searchViewModel.tagRanking.collectAsState()
    val tagRecommendTrack by searchViewModel.tagRecommendTrack.collectAsState()
    val tagRecentTrack by searchViewModel.tagRecentTrack.collectAsState()

    Scaffold(
        topBar = {
            CommonAppBar(
                title = "#$tagName",
                navController = navController,
                logoutManager = logoutManager,
                coroutineScope = coroutineScope,
                showBackButton = true
            )
        }
    ) { innerPadding ->
        LazyColumn(
            Modifier
                .padding(innerPadding)
                .fillMaxWidth()
        ) {
            // 추천 트랙
            item {
                Text(
                    text = "\"$tagName\" for YOU",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    style = Typography.titleLarge,
                    color = CustomColors().Grey50,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                if (tagRecommendTrack.size < 3) {
                    Text(
                        text = "We can't recommend tracks.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        style = Typography.titleSmall,
                        color = CustomColors().Grey200,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    return@item
                }
                val tracks = tagRecommendTrack.map {
                    TrackEssential(
                        trackId = it.trackId,
                        title = it.title,
                        artist = it.nickname,
                        imageUrl = it.imageUrl
                    )
                }
                TrackListRow(modifier = Modifier,
                    trackList = tracks,
                    workStationViewModel = workStationViewModel,
                    navController = navController
                )
            }
            // 랭킹
            item {
                Text(
                    text = "Best Tracks of \"$tagName\"",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    style = Typography.titleLarge,
                    color = CustomColors().Grey50,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                if (tagRanking.isEmpty()) {
                    Text(
                        text = "No tracks found",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        style = Typography.titleSmall,
                        color = CustomColors().Grey200,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    return@item
                }
            }
            items(tagRanking.size) { index ->
                val track = tagRanking[index]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TrackItemRow(
                        track = TrackEssential(
                            trackId = track.trackId,
                            title = track.title,
                            artist = track.nickname,
                            imageUrl = track.imageUrl
                        ),
                        style = TrackItemStyle.RANKING,
                        rank = index + 1,
                        trackPlayViewModel = trackPlayViewModel,
                        workStationViewModel = workStationViewModel,
                        navController = navController,
                        needImportButton = true
                    )
                }
            }
            // 최근 올라온 트랙
            item {
                Text(
                    text = "Recent Tracks of \"$tagName\"",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    style = Typography.titleLarge,
                    color = CustomColors().Grey50,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                if (tagRecentTrack.isEmpty()) {
                    Text(
                        text = "No tracks found",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        style = Typography.titleSmall,
                        color = CustomColors().Grey200,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    return@item
                }
            }
            items(tagRecentTrack.size) { index ->
                val track = tagRecentTrack[index]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TrackItemRow(
                        track = TrackEssential(
                            trackId = track.trackId,
                            title = track.title,
                            artist = track.nickname,
                            imageUrl = track.imageUrl
                        ),
                        workStationViewModel = workStationViewModel,
                        navController = navController,
                        needImportButton = true
                    )
                }
            }
            // 하단 여백
            item {
                Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding()))
            }
        }
    }
}