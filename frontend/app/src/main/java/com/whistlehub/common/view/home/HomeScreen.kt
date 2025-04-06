package com.whistlehub.common.view.home

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.whistlehub.R
import com.whistlehub.common.data.remote.dto.response.TrackResponse
import com.whistlehub.common.util.LogoutManager
import com.whistlehub.common.view.component.CommonAppBar
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.common.view.track.NewTrackCard
import com.whistlehub.common.view.track.TrackItemRow
import com.whistlehub.common.view.track.TrackListRow
import com.whistlehub.playlist.data.TrackEssential
import com.whistlehub.playlist.viewmodel.TrackPlayViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun HomeScreen(
    paddingValues: PaddingValues,
    trackPlayViewModel: TrackPlayViewModel = hiltViewModel(),
    navController: NavHostController,
    logoutManager: LogoutManager
) {
    val coroutineScope = rememberCoroutineScope()

    // 최근 올라온 트랙 (임시)
    val newList by trackPlayViewModel.trackList.collectAsState(initial = emptyList())
    // 최근 들은 느낌의 트랙
    var recentList by remember { mutableStateOf<List<TrackEssential>>(emptyList()) }
    // 트랙과 비슷한 느낌의 트랙
    var similarList by remember { mutableStateOf<List<TrackEssential>>(emptyList()) }
    // 한 번도 안 들어본 음악
    var notListenedList by remember { mutableStateOf<List<TrackEssential>>(emptyList()) }
    // 팔로워 중 한 명 선택
    var selectedFollowing by remember { mutableStateOf<TrackResponse.MemberInfo?>(null) }
    // 팔로잉 팬믹스 추천
    var fanmix by remember { mutableStateOf<List<TrackEssential>>(emptyList()) }

    LaunchedEffect(Unit) {
        trackPlayViewModel.getTempTrackList() // 최근 올라온 트랙(임시) 가져오기
        recentList = trackPlayViewModel.getRecentTrackList() // 최근 들은 느낌의 트랙 가져오기
        notListenedList = trackPlayViewModel.getNeverTrackList() // 한 번도 안 들어본 음악 가져오기
        selectedFollowing = trackPlayViewModel.getFollowingMember() // 팔로워 중 한 명 선택
        fanmix = trackPlayViewModel.getFanMixTracks(selectedFollowing?.memberId ?: 0) // 팬믹스 가져오기
    }

    var showSimlarTrackSheet by remember { mutableStateOf(false) }
    var showFanmixSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 최근 올라온 트랙
            item {
                Text(
                    text = "최근 올라온 트랙",
                    style = Typography.titleLarge,
                    modifier = Modifier.padding(start = 10.dp, top = 10.dp, end = 10.dp)
                )
                if (newList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("최근 올라온 트랙이 없습니다.")
                    }
                    return@item
                }
                LazyRow(
                    Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(newList.size) { index ->
                        var trackData by remember {
                            mutableStateOf<TrackResponse.GetTrackDetailResponse?>(
                                null
                            )
                        }
                        LaunchedEffect(Unit) {
                            trackData = trackPlayViewModel.getTrackbyTrackId(newList[index].trackId)
                        }
                        trackData?.let { track ->
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
                            .padding(10.dp)
                    ) {
                        Text(
                            "최근 들은 느낌의 트랙",
                            style = Typography.titleLarge,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (recentList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("정보를 가져올 수 없습니다.")
                        }
                        return@item
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalArrangement = Arrangement.spacedBy(15.dp),
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(3) { index ->
                            if (index < recentList.size) {
                                val track = recentList[index]
                                Column(
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            coroutineScope.launch {
                                                similarList =
                                                    trackPlayViewModel.getSimilarTrackList(track.trackId)
                                                if (similarList.isEmpty()) {
                                                    return@launch
                                                }
                                                showSimlarTrackSheet = true
                                            }
                                        },
                                    verticalArrangement = Arrangement.spacedBy(
                                        5.dp,
                                        alignment = Alignment.Top
                                    ),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(0.9f),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        Box(
                                            Modifier
                                                .padding(start = 10.dp, end = 10.dp)
                                                .background(
                                                    CustomColors().CommonButtonColor,
                                                    RoundedCornerShape(5.dp)
                                                )
                                                .fillMaxWidth()
                                                .fillMaxHeight()
                                        )
                                        Box(
                                            Modifier
                                                .padding(start = 5.dp, end = 5.dp, top = 5.dp)
                                                .background(
                                                    CustomColors().CommonSubBackgroundColor,
                                                    RoundedCornerShape(5.dp)
                                                )
                                                .fillMaxWidth()
                                                .fillMaxHeight()
                                        )
                                        AsyncImage(
                                            model = track.imageUrl,
                                            contentDescription = "Track Image",
                                            modifier = Modifier
                                                .padding(top = 10.dp)
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(5.dp)),
                                            error = painterResource(R.drawable.default_track),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Text(
                                        text = "Similar with",
                                        style = Typography.bodySmall,
                                        color = CustomColors().CommonSubTextColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                    )
                                    Text(
                                        text = track.title,
                                        style = Typography.titleMedium,
                                        lineHeight = 18.sp,
                                        color = CustomColors().CommonTextColor,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                    )
                                }
                            } else {
                                Box(Modifier.size(100.dp)) {
                                    Text("No Data")
                                }
                            }
                        }
                    }
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
                            "한번도 안 들어본 트랙",
                            style = Typography.titleLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Rounded.Replay,
                            contentDescription = "Replay",
                            tint = CustomColors().CommonIconColor,
                            modifier = Modifier.clickable {
                                coroutineScope.launch {
                                    notListenedList = trackPlayViewModel.getNeverTrackList() // 갱신
                                }
                            }
                        )
                    }
                    if (notListenedList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("정보를 가져올 수 없습니다.")
                        }
                        return@item
                    }
                    TrackListRow(trackList = notListenedList)
                }
            }

            // 팔로잉 팬믹스 추천
            item {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = selectedFollowing?.profileImage,
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            error = painterResource(R.drawable.default_profile),
                            contentScale = ContentScale.Crop
                        )
                        Column(
                            Modifier
                                .padding(10.dp)
                                .weight(1f)
                        ) {
                            Text(
                                "FanMix for follower",
                                style = Typography.bodyMedium,
                                color = CustomColors().CommonSubTextColor,
                            )
                            Text(
                                text = selectedFollowing?.nickname ?: "Unknown",
                                style = Typography.titleMedium,
                                color = CustomColors().CommonTextColor,
                            )
                        }
                        Text(
                            text = "더보기",
                            style = Typography.bodyMedium,
                            color = CustomColors().CommonSubTextColor,
                            modifier = Modifier.clickable {
                                coroutineScope.launch {
                                    if (fanmix.size < 4) {
                                        return@launch
                                    }
                                    showFanmixSheet = true
                                }
                            }
                        )
                    }
                    if (fanmix.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("정보를 가져올 수 없습니다.")
                        }
                        return@item
                    }
                    TrackListRow(trackList = fanmix)
                }
            }

            // 최하단 Space
            item {
                Spacer(
                    Modifier.height(paddingValues.calculateBottomPadding())
                )
            }
        }

        if (showSimlarTrackSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSimlarTrackSheet = false },
                sheetState = sheetState,
                shape = RoundedCornerShape(16.dp),
                containerColor = CustomColors().CommonBackgroundColor,
                content = {
                    LazyColumn {
                        items(similarList.size) { index ->
                            val track = similarList[index]
                            TrackItemRow(track)
                        }
                    }
                }
            )
        }

        if (showFanmixSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFanmixSheet = false },
                sheetState = sheetState,
                shape = RoundedCornerShape(16.dp),
                containerColor = CustomColors().CommonBackgroundColor,
                content = {
                    LazyColumn {
                        items(fanmix.size) { index ->
                            val track = fanmix[index]
                            TrackItemRow(track)
                        }
                    }
                }
            )
        }
    }
}