package com.whistlehub.profile.view

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.whistlehub.common.data.remote.dto.request.ProfileRequest
import com.whistlehub.common.data.remote.dto.response.ProfileResponse
import com.whistlehub.common.util.LogoutManager
import com.whistlehub.common.view.navigation.Screen
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.profile.viewmodel.ProfileViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

enum class FollowListType {
    FOLLOWERS,
    FOLLOWING
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    logoutManager: LogoutManager,
    memberIdParam: Int?,
    navController: NavHostController
) {
    val customColors = CustomColors()
    // 로그인한 유저의 memberId는 ViewModel에서 가져옵니다.
    val currentUserId by viewModel.memberId.collectAsState()
    // 로그인한 유저의 프로필을 보여줄 경우, memberIdParam이 null이면 ViewModel 내에서 로컬 저장된 값을 사용
    val memberId = memberIdParam ?: currentUserId

    val profile by viewModel.profile.collectAsState()
    val tracks by viewModel.tracks.collectAsState()
    val followerList by viewModel.followers.collectAsState()
    val followingList by viewModel.followings.collectAsState()
    val isFollowing by viewModel.isFollowing.collectAsState()

    var currentPage by remember { mutableIntStateOf(0) }
    var currentFollowPage by remember { mutableIntStateOf(0) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var isLoadingFollowers by remember { mutableStateOf(false) }
    var hasMoreFollowers by remember { mutableStateOf(true) }
    var hasMoreFollowing by remember { mutableStateOf(true) }

    val errorMessage by viewModel.errorMessage.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // 팔로우 타입 (팔로워 목록 또는 팔로잉 목록)
    var currentFollowListType by remember { mutableStateOf<FollowListType?>(null) }

    // ModalBottomSheet 상태
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    // LazyVerticalGrid의 스크롤 상태
    val gridState: LazyGridState = rememberLazyGridState()
    val followerListState = rememberLazyListState()
    val followingListState = rememberLazyListState()

    // 화면이 처음 구성될 때 프로필 데이터를 로드합니다.
    LaunchedEffect(memberId) {
        viewModel.loadProfile(memberId)
        viewModel.loadTracks(memberId, page = 0, size = 9)
        currentPage = 0

        // 팔로우 여부 확인 (자신의 프로필이 아닌 경우에만)
        if (memberId != currentUserId) {
            viewModel.checkFollowStatus(memberId)
        }
    }

    // 스크롤 상태를 감시해서 마지막 아이템이 보이면 다음 페이지를 로드
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .map { it ?: 0 }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex >= tracks.size - 1 && !isLoadingMore) {
                    isLoadingMore = true
                    currentPage++
                    viewModel.loadTracks(memberId, page = currentPage, size = 9)
                    isLoadingMore = false
                }
            }
    }

    // 팔로워 목록 스크롤 감시
    LaunchedEffect(followerListState) {
        snapshotFlow { followerListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .map { it ?: 0 }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex >= followerList.size - 2 && hasMoreFollowers && !isLoadingFollowers) {
                    isLoadingFollowers = true
                    val nextPage = currentFollowPage + 1
                    viewModel.loadFollowers(memberId, page = nextPage, size = 15)
                    // 응답 데이터가 요청 size보다 작으면 더 이상 데이터가 없는 것으로 판단
                    if (followerList.size % 15 != 0) {
                        hasMoreFollowers = false
                    } else {
                        currentFollowPage = nextPage
                    }
                    isLoadingFollowers = false
                }
            }
    }

    // 팔로잉 목록 스크롤 감시
    LaunchedEffect(followingListState) {
        snapshotFlow { followingListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .map { it ?: 0 }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex >= followingList.size - 2 && hasMoreFollowing && !isLoadingFollowers) {
                    isLoadingFollowers = true
                    val nextPage = currentFollowPage + 1
                    viewModel.loadFollowings(memberId, page = nextPage, size = 15)
                    // 응답 데이터가 요청 size보다 작으면 더 이상 데이터가 없는 것으로 판단
                    if (followingList.size % 15 != 0) {
                        hasMoreFollowing = false
                    } else {
                        currentFollowPage = nextPage
                    }
                    isLoadingFollowers = false
                }
            }
    }

    // Main content
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Whistle Hub", color = customColors.Grey50) },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.ProfileMenu.route)
                    }) {
                        Icon(Icons.Default.Menu, contentDescription = "메뉴", tint = customColors.Grey50)
                    }
                    IconButton(onClick = {
                        coroutineScope.launch {
                            logoutManager.emitLogout()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "로그아웃", tint = customColors.Grey50)
                    }
                }
            )
        },
    ) { innerPadding ->
        // Display the bottom sheet if needed
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = bottomSheetState,
                containerColor = customColors.Grey900,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (currentFollowListType == FollowListType.FOLLOWERS) "Followers" else "Following",
                        style = MaterialTheme.typography.titleLarge,
                        color = customColors.Grey50,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (currentFollowListType == FollowListType.FOLLOWERS) {
                        if (followerList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No followers yet",
                                    color = customColors.Grey300
                                )
                            }
                        } else {
                            LazyColumn(
                                state = followerListState,
                                modifier = Modifier.height(400.dp)
                            ) {
                                items(followerList) { follower ->
                                    FollowListItem(
                                        profileImage = follower.profileImage,
                                        nickname = follower.nickname,
                                        onClick = {
                                            showBottomSheet = false
                                            navController.navigate("profile/${follower.memberId}")
                                        }
                                    )
                                }

                                if (hasMoreFollowers && followerList.isNotEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = customColors.Mint500)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (followingList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Not following anyone yet",
                                    color = customColors.Grey300
                                )
                            }
                        } else {
                            LazyColumn(
                                state = followingListState,
                                modifier = Modifier.height(400.dp)
                            ) {
                                items(followingList) { following ->
                                    FollowListItem(
                                        profileImage = following.profileImage,
                                        nickname = following.nickname,
                                        onClick = {
                                            showBottomSheet = false
                                            navController.navigate("profile/${following.memberId}")
                                        }
                                    )
                                }

                                if (hasMoreFollowing && followingList.isNotEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = customColors.Mint500)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Add padding at the bottom for better spacing
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(3),
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 프로필 헤더를 그리드의 첫 아이템으로 추가 (전체 폭 사용)
            item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                ProfileHeader(
                    profileImage = profile?.profileImage,
                    nickname = profile?.nickname ?: "Loading...",
                    profileText = profile?.profileText ?: "",
                    followerCount = followerList.size,
                    followingCount = followingList.size,
                    trackCount = tracks.size,
                    showFollowButton = memberId != currentUserId,
                    isFollowing = isFollowing,
                    onFollowClick = {
                        coroutineScope.launch {
                            viewModel.toggleFollow(memberId)
                        }
                    },
                    onFollowersClick = {
                        coroutineScope.launch {
                            currentFollowListType = FollowListType.FOLLOWERS
                            currentFollowPage = 0
                            hasMoreFollowers = true
                            // 팔로워 목록 로드
                            viewModel.loadFollowers(memberId, page = 0, size = 15)
                            showBottomSheet = true
                        }
                    },
                    onFollowingClick = {
                        coroutineScope.launch {
                            currentFollowListType = FollowListType.FOLLOWING
                            currentFollowPage = 0
                            hasMoreFollowing = true
                            // 팔로잉 목록 로드
                            viewModel.loadFollowings(memberId, page = 0, size = 15)
                            showBottomSheet = true
                        }
                    }
                )
            }
            // 트랙 아이템들 렌더링
            items(count = tracks.size) { index ->
                val track = tracks[index]
                TrackGridItem(track = track, onClick = { /* 트랙 클릭 처리 */ })
            }
            // 로딩 인디케이터 (옵션)
            if (isLoadingMore) {
                item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = customColors.Mint500)
                    }
                }
            }
        }
    }
}

@Composable
fun FollowListItem(
    profileImage: String?,
    nickname: String,
    onClick: () -> Unit
) {
    val customColors = CustomColors()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!profileImage.isNullOrEmpty()) {
            AsyncImage(
                model = profileImage,
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(customColors.Grey700),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Image",
                    tint = customColors.Grey300,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = nickname,
            style = MaterialTheme.typography.bodyLarge,
            color = customColors.Grey50
        )
    }
}

@Composable
fun TrackGridItem(
    track: ProfileResponse.GetMemberTracksResponse,
    onClick: () -> Unit
) {
    val customColors = CustomColors()

    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        track.imageUrl?.let {
            AsyncImage(
                model = it,
                contentDescription = track.title,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        } ?: Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(customColors.Grey800),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "Music",
                tint = customColors.Grey500,
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = track.title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            color = customColors.Grey200
        )
    }
}

@Composable
fun ProfileHeader(
    profileImage: String?,
    nickname: String,
    profileText: String,
    followerCount: Int,
    followingCount: Int,
    trackCount: Int = 0,
    showFollowButton: Boolean = false,
    isFollowing: Boolean = false,
    onFollowClick: () -> Unit = {},
    onFollowersClick: () -> Unit = {},
    onFollowingClick: () -> Unit = {}
) {
    val colors = CustomColors()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 프로필 이미지, 닉네임, 자기소개 영역
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!profileImage.isNullOrEmpty()) {
                AsyncImage(
                    model = profileImage,
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Image",
                        tint = Color.LightGray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = nickname,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.Grey50
                )
                Text(
                    text = profileText,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    color = colors.Grey50
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // 팔로우 버튼 영역
        if (showFollowButton) {
            Button(
                onClick = onFollowClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowing) colors.Grey700 else colors.Mint500,
                    contentColor = colors.Grey50
                )
            ) {
                Text(text = if (isFollowing) "팔로잉" else "팔로우")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        // 트랙, 팔로워, 팔로잉 통계 영역
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ProfileStat(
                statLabel = "Tracks",
                statValue = formatNumber(trackCount)
            )
            ProfileStat(
                statLabel = "Followers",
                statValue = formatNumber(followerCount),
                onClick = onFollowersClick
            )
            ProfileStat(
                statLabel = "Following",
                statValue = formatNumber(followingCount),
                onClick = onFollowingClick
            )
        }
    }
}

fun formatNumber(number: Int): String {
    return when {
        number >= 1_000_000_000 -> "${number / 1_000_000_000}b+"
        number >= 1_000_000 -> "${number / 1_000_000}m+"
        number >= 1_000 -> "${number / 1_000}k+"
        else -> number.toString()
    }
}

@Composable
fun ProfileStat(
    statLabel: String,
    statValue: String,
    onClick: () -> Unit = {}
) {
    val customColors = CustomColors()
    Box(
        modifier = Modifier
            .padding(8.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = statValue,
                style = MaterialTheme.typography.titleLarge,
                color = customColors.Grey50
            )
            Text(
                text = statLabel,
                style = MaterialTheme.typography.bodySmall,
                color = customColors.Grey50
            )
        }
    }
}

@Composable
fun CircleIconButton(
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}