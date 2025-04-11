package com.whistlehub.common.view.home

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
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
import com.whistlehub.common.view.navigation.Screen
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.common.view.track.NewTrackCard
import com.whistlehub.common.view.track.TrackItemRow
import com.whistlehub.common.view.track.TrackListRow
import com.whistlehub.playlist.data.TrackEssential
import com.whistlehub.playlist.viewmodel.TrackPlayViewModel
import com.whistlehub.workstation.viewmodel.WorkStationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("StateFlowValueCalledInComposition", "UnusedBoxWithConstraintsScope")
@Composable
fun HomeScreen(
    paddingValues: PaddingValues,
    trackPlayViewModel: TrackPlayViewModel = hiltViewModel(),
    workStationViewModel: WorkStationViewModel,
    navController: NavHostController,
    logoutManager: LogoutManager
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // 최근 올라온 트랙
    var newList by remember { mutableStateOf<List<TrackEssential>>(emptyList()) }
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
    // 공식 계정 트랙
    var officialList by remember { mutableStateOf<List<TrackEssential>>(emptyList()) }
    // 타겟 트랙
    var targetTrack by remember { mutableStateOf<TrackEssential?>(null) }

    LaunchedEffect(newList) {
        listState.animateScrollToItem(0)
    }

    LaunchedEffect(Unit) {
        newList = trackPlayViewModel.getFollowRecentTracks() // 최근 올라온 트랙 가져오기
        recentList = trackPlayViewModel.getRecentTrackList() // 최근 들은 느낌의 트랙 가져오기
        notListenedList = trackPlayViewModel.getNeverTrackList() // 한 번도 안 들어본 음악 가져오기
        selectedFollowing = trackPlayViewModel.getFollowingMember() // 팔로워 중 한 명 선택
        fanmix = trackPlayViewModel.getFanMixTracks(selectedFollowing?.memberId ?: 0) // 팬믹스 가져오기
    }

    var showSimlarTrackSheet by remember { mutableStateOf(false) }
    var showFanmixSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // 최신 트랙 상태관리
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val spacingDp = 10.dp
    val spacingPx = with(density) { spacingDp.toPx() }
    var currentIndex by remember { mutableStateOf(0) }

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
            verticalArrangement = Arrangement.spacedBy(20.dp),
            state = listState
        ) {
            // 최근 올라온 트랙
            item {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Text(
                            "최근 올라온 트랙",
                            style = Typography.titleLarge,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                // 자동 슬라이딩 카드
                Column {
                    if (newList.isEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Official 채널을 팔로우하고 최신 트랙을 확인해보세요!",
                                style = Typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowForwardIos,
                                contentDescription = "Go to Official Channel",
                                tint = CustomColors().CommonIconColor,
                                modifier = Modifier.clickable {
                                    navController.navigate(Screen.Profile.route + "/1")
                                }
                            )
                        }
                    }
                    BoxWithConstraints (
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        val screenWidth = constraints.maxWidth.toFloat()
                        val cardPx = screenWidth // 카드 한 장이 전체 너비를 차지
                        val totalPxPerCard = cardPx + spacingPx

                        // 자동 슬라이딩
                        LaunchedEffect(newList.size) {
                            if (newList.isNotEmpty()) {
                                while (true) {
                                    delay(3000L)
                                    val nextIndex = (currentIndex + 1) % newList.size
                                    currentIndex = nextIndex
                                    scrollState.animateScrollTo((nextIndex * totalPxPerCard).toInt())
                                }
                            } else {
                                officialList = trackPlayViewModel.getMemberTracks(1)  // 오피셜 계정의 최신 목록 가져오기
                            }
                        }
                        Row(
                            modifier = Modifier
                                .horizontalScroll(scrollState, enabled = false),
                            horizontalArrangement = Arrangement.spacedBy(spacingDp)
                        ) {
                            val targetlist = if (newList.isEmpty()) officialList else newList
                            targetlist.forEach { track ->
                                val trackData by produceState<TrackResponse.GetTrackDetailResponse?>(null) {
                                    value = trackPlayViewModel.getTrackbyTrackId(track.trackId)
                                }

                                trackData?.let { data ->
                                    Box(
                                        modifier = Modifier
                                            .width(with(density) { cardPx.toDp() })
                                            .padding(5.dp)
                                    ) {
                                        NewTrackCard(
                                            track = data,
                                            trackPlayViewModel = trackPlayViewModel,
                                            navController = navController,
                                            workStationViewModel = workStationViewModel
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        newList.forEachIndexed { index, _ ->
                            Box(
                                modifier = Modifier
                                    .size(if (index == currentIndex) 10.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(if (index == currentIndex) CustomColors().CommonIconColor else CustomColors().CommonButtonColor)
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
                            Text("최근 들은 곡이 없습니다. 새로운 곡을 들어보세요!")
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
                                                targetTrack = track
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.618f)
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp, alignment = Alignment.CenterHorizontally)
                        ) {
                            Text(
                                "모든 트랙을 다 들어봤나요? 직접 트랙을 만들어보는 건 어때요?",
                                style = Typography.titleMedium
                            )
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowForwardIos,
                                contentDescription = "Go to Official Channel",
                                tint = CustomColors().CommonIconColor,
                                modifier = Modifier.clickable {
                                    navController.navigate(Screen.DAW.route)
                                }
                            )
                        }
                        return@item
                    }
                    TrackListRow(trackList = notListenedList,
                        workStationViewModel = workStationViewModel,
                        navController = navController
                    )
                }
            }

            // 팔로잉 팬믹스 추천
            item {
                if (fanmix.isEmpty()) return@item
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
                                .clip(RoundedCornerShape(5.dp))
                                .clickable {
                                    navController.navigate(Screen.Profile.route + "/${selectedFollowing?.memberId}")
                                },
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
                        if (fanmix.size > 3) {
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
                    }
                    TrackListRow(trackList = fanmix,
                        workStationViewModel = workStationViewModel,
                        navController = navController
                    )
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
                        item {
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = targetTrack?.imageUrl,
                                    contentDescription = "Playlist Cover",
                                    modifier = Modifier
                                        .size(150.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop,
                                    error = painterResource(id = R.drawable.default_track)
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "similar with",
                                    style = Typography.bodyMedium,
                                    color = CustomColors().CommonSubTitleColor,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(5.dp))
                                Text(
                                    targetTrack?.title!!,
                                    style = Typography.titleLarge,
                                    color = CustomColors().CommonTextColor
                                )
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                        items(similarList.size) { index ->
                            val track = similarList[index]
                            TrackItemRow(
                                track,
                                workStationViewModel = workStationViewModel,
                                navController = navController
                            )
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
                        item {
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 24.dp)
                                    .clickable {
                                        navController.navigate(Screen.Profile.route + "/${selectedFollowing?.memberId}")
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = selectedFollowing?.profileImage,
                                    contentDescription = "Playlist Cover",
                                    modifier = Modifier
                                        .size(150.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop,
                                    error = painterResource(id = R.drawable.default_profile)
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "FanMix for follower",
                                    style = Typography.bodyMedium,
                                    color = CustomColors().CommonSubTitleColor,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(5.dp))
                                Text(
                                    selectedFollowing?.nickname!!,
                                    style = Typography.titleLarge,
                                    color = CustomColors().CommonTextColor
                                )
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                        items(fanmix.size) { index ->
                            val track = fanmix[index]
                            TrackItemRow(
                                track,
                                workStationViewModel = workStationViewModel,
                                navController = navController
                            )
                        }
                    }
                }
            )
        }
    }
}