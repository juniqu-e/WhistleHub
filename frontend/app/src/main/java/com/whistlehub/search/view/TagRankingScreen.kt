package com.whistlehub.search.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.common.view.track.TrackItemRow
import com.whistlehub.common.view.track.TrackListRow
import com.whistlehub.playlist.data.TrackEssential
import com.whistlehub.playlist.viewmodel.TrackPlayViewModel
import com.whistlehub.search.viewmodel.SearchViewModel

@Composable
fun TagRankingScreen(
    tagId: Int,
    tagName: String,
    paddingValues: PaddingValues,
    trackPlayViewModel: TrackPlayViewModel = hiltViewModel(),
    searchViewModel: SearchViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        searchViewModel.getRankingByTag(tagId, "WEEK")
    }
    val tagRanking = searchViewModel.tagRanking.collectAsState().value

    LazyColumn(Modifier.fillMaxWidth()) {
        // 태그 제목
        item {
            Text(
                text = "#$tagName",
                modifier = Modifier
                    .fillMaxWidth(),
                style = Typography.displaySmall,
                color = CustomColors().Grey50,
                fontWeight = FontWeight.Bold
            )
        }

        // 태그 Top 3 (가로 배열)
        item {
            if (tagRanking.size < 3) {
                Text(
                    text = "No ranking data available",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    style = Typography.titleLarge,
                    color = CustomColors().Grey50,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                return@item
            }
            val trackTop3 = tagRanking.map {
                TrackEssential(
                    trackId = it.trackId,
                    title = it.title,
                    artist = it.nickname,
                    imageUrl = it.imageUrl
                )
            }.take(3)  // 처음 3개만 가져옴
            Column {
                Text(
                    text = "Top 3 This Week",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    style = Typography.titleLarge,
                    color = CustomColors().Grey50,
                    fontWeight = FontWeight.Bold
                )
                TrackListRow(trackList = trackTop3)
            }
        }
        if (tagRanking.size < 4) {
            return@LazyColumn
        }

        // 태그 4위 ~ 50위 (세로 배열)
        item {
            Text(
                text = "Best Tracs of \"$tagName\"",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                style = Typography.titleLarge,
                color = CustomColors().Grey50,
                fontWeight = FontWeight.Bold
            )
        }
        items(tagRanking.size - 3) { index ->
            val track = tagRanking[index + 3]  // 4위부터 시작
            TrackItemRow(
                track = TrackEssential(
                    trackId = track.trackId,
                    title = track.title,
                    artist = track.nickname,
                    imageUrl = track.imageUrl
                ),
                trackPlayViewModel = trackPlayViewModel,
            )
        }

        // 하단 여백
        item {
            Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding()))
        }
    }
}