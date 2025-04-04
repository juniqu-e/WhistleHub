package com.whistlehub.profile.view

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.whistlehub.common.util.LogoutManager
import com.whistlehub.common.view.component.CommonAppBar
import com.whistlehub.common.view.navigation.Screen
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.playlist.viewmodel.TrackPlayViewModel
import com.whistlehub.profile.view.components.ProfileFollowSheet
import com.whistlehub.profile.view.components.ProfileHeader
import com.whistlehub.profile.view.components.ProfileSearchBar
import com.whistlehub.profile.view.components.ProfileTrackDetailSheet
import com.whistlehub.profile.view.components.TrackGridItem
import com.whistlehub.profile.viewmodel.ProfileTrackDetailViewModel
import com.whistlehub.profile.viewmodel.ProfileViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * 팔로워/팔로잉 목록 타입을 구분하는 열거형
 */
enum class FollowListType {
    FOLLOWERS,
    FOLLOWING
}

/**
 * 사용자 프로필 화면
 * 프로필 정보, 검색, 트랙 목록, 팔로워/팔로잉 정보를 표시합니다.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(
    memberIdParam: Int?,
    paddingValues: PaddingValues,
    logoutManager: LogoutManager,
    navController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel(),
    trackPlayViewModel: TrackPlayViewModel = hiltViewModel()
) {
    val customColors = CustomColors()
    val coroutineScope = rememberCoroutineScope()

    // 로그인한 유저의 memberId는 ViewModel에서 가져옵니다.
    val currentUserId by viewModel.memberId.collectAsState()
    // 로그인한 유저의 프로필을 보여줄 경우, memberIdParam이 null이면 ViewModel 내에서 로컬 저장된 값을 사용
    val memberId = memberIdParam ?: currentUserId

    // ViewModel 상태 수집
    val profile by viewModel.profile.collectAsState()
    val tracks by viewModel.tracks.collectAsState()
    val followerList by viewModel.followers.collectAsState()
    val followingList by viewModel.followings.collectAsState()
    val followerCount by viewModel.followerCount.collectAsState()
    val followingCount by viewModel.followingCount.collectAsState()
    val isFollowing by viewModel.isFollowing.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // 지역 상태 관리
    var currentPage by remember { mutableIntStateOf(0) }
    var currentFollowPage by remember { mutableIntStateOf(0) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var isLoadingFollowers by remember { mutableStateOf(false) }

    // 팔로우 타입 (팔로워 목록 또는 팔로잉 목록)
    var currentFollowListType by remember { mutableStateOf<FollowListType?>(null) }

    val hasMoreFollowersState by viewModel.hasMoreFollowers.collectAsState()
    val hasMoreFollowingsState by viewModel.hasMoreFollowings.collectAsState()

    // ModalBottomSheet 상태
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }

    // LazyVerticalGrid의 스크롤 상태
    val gridState: LazyGridState = rememberLazyGridState()

    var selectedTrackId by remember { mutableStateOf<Int?>(null) }
    var showTrackDetailSheet by remember { mutableStateOf(false) }
    val trackDetailViewModel: ProfileTrackDetailViewModel = hiltViewModel()
    val trackDetail by trackDetailViewModel.trackDetail.collectAsState()

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

    // 팔로우 상태 변경 효과 추적
    LaunchedEffect(isFollowing) {
        Log.d("ProfileScreen", "Follow status changed: $isFollowing")
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

    // Main content
    Scaffold(
        topBar = {
            CommonAppBar(
                title = "Whistle Hub",
                navController = navController,
                logoutManager = logoutManager,
                coroutineScope = coroutineScope,
            )
        },
        bottomBar = {
            Spacer(
                Modifier
                .height(paddingValues.calculateBottomPadding())
            )
        }
    ) { innerPadding ->
        // Display the bottom sheet if needed
        if (showBottomSheet && currentFollowListType != null) {
            ProfileFollowSheet(
                followListType = currentFollowListType!!,
                followers = followerList,
                followings = followingList,
                hasMoreFollowers = hasMoreFollowersState,
                hasMoreFollowing = hasMoreFollowingsState,
                onDismiss = { showBottomSheet = false },
                onUserClick = { userId ->
                    showBottomSheet = false
                    navController.navigate("${Screen.Profile.route}/$userId")
                },
                sheetState = bottomSheetState,
                currentUserId = currentUserId
            )
        }

        // Scaffold에서 제공하는 innerPadding과 BottomNavigationBar 패딩 모두 고려
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(3),
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(
                start = 8.dp,
                top = 8.dp,
                end = 8.dp,
                bottom = 8.dp // 하단 네비게이션 높이만큼 패딩 추가
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 검색창을 그리드의 첫 아이템으로 추가
            item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                ProfileSearchBar(
                    viewModel = viewModel,
                    onUserSelected = { userId ->
                        navController.navigate("${Screen.Profile.route}/$userId")
                    }
                )
            }

            // 프로필 헤더를 그리드의 다음 아이템으로 추가 (전체 폭 사용)
            item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                ProfileHeader(
                    profileImage = profile?.profileImage,
                    nickname = profile?.nickname ?: "Loading...",
                    profileText = profile?.profileText ?: "",
                    followerCount = followerCount,
                    followingCount = followingCount,
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
                            // 팔로워 목록 로드 (페이지네이션용)
                            viewModel.loadFollowers(memberId, page = 0, size = 15)
                            showBottomSheet = true
                        }
                    },
                    onFollowingClick = {
                        coroutineScope.launch {
                            currentFollowListType = FollowListType.FOLLOWING
                            currentFollowPage = 0
                            // 팔로잉 목록 로드 (페이지네이션용)
                            viewModel.loadFollowings(memberId, page = 0, size = 15)
                            showBottomSheet = true
                        }
                    }
                )
            }

            // 트랙 아이템들 렌더링
            items(count = tracks.size) { index ->
                val track = tracks[index]

                Box(
                    modifier = Modifier
                        .combinedClickable(
                            onClick = {
                                // 트랙 클릭 시 수행할 작업 (예: 재생)
                                coroutineScope.launch {
                                    trackPlayViewModel.playTrack(track.trackId)
                                }
                            },
                            onLongClick = {
                                // 트랙 길게 클릭 시 수행할 작업 (예: 상세 정보 표시)
                                selectedTrackId = track.trackId
                                trackDetailViewModel.loadTrackDetails(track.trackId)
                                showTrackDetailSheet = true
                            }
                        )
                ) {
                    // 기존 TrackGridItem 컴포넌트 사용 (수정 없이)
                    TrackGridItem(
                        track = track
                    )
                }
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

        // 트랙 상세 정보 바텀 시트
        if (showTrackDetailSheet && trackDetail != null) {
            ProfileTrackDetailSheet(
                track = trackDetail!!,
                isOwnProfile = memberId == currentUserId,
                onDismiss = {
                    showTrackDetailSheet = false
                    selectedTrackId = null
                },
                viewModel = trackDetailViewModel
            )
        }
    }
}