package com.whistlehub.profile.view

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.whistlehub.common.util.LogoutManager
import com.whistlehub.common.view.navigation.Screen
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.profile.view.components.ProfileFollowSheet
import com.whistlehub.profile.view.components.ProfileHeader
import com.whistlehub.profile.view.components.ProfileSearchBar
import com.whistlehub.profile.view.components.TrackGridItem
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    memberIdParam: Int?,
    logoutManager: LogoutManager,
    navController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val customColors = CustomColors()

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
    val coroutineScope = rememberCoroutineScope()

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
            TopAppBar(
                title = { Text("Whistle Hub", style = Typography.titleLarge, color = customColors.Grey50) },
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
        if (showBottomSheet && currentFollowListType != null) {
            ProfileFollowSheet(
                followListType = currentFollowListType!!,
                followers = followerList,
                followings = followingList,
                hasMoreFollowers = hasMoreFollowersState,  // ViewModel의 상태 사용
                hasMoreFollowing = hasMoreFollowingsState,  // ViewModel의 상태 사용
                onDismiss = { showBottomSheet = false },
                onUserClick = { userId ->
                    showBottomSheet = false
                    navController.navigate("${Screen.Profile.route}/$userId")
                },
                sheetState = bottomSheetState,
                currentUserId = currentUserId
            )
        }

        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(3),
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(8.dp),
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