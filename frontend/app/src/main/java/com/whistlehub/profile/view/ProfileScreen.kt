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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.whistlehub.common.data.remote.dto.response.ProfileResponse
import com.whistlehub.common.util.LogoutManager
import com.whistlehub.common.view.navigation.Screen
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.common.viewmodel.LoginViewModel
import com.whistlehub.profile.viewmodel.ProfileViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

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
    var currentPage by remember { mutableIntStateOf(0) }

    val errorMessage by viewModel.errorMessage.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // 로딩 상태 플래그
    var isLoading by remember { mutableStateOf(false) }

    // LazyVerticalGrid의 스크롤 상태
    val gridState: LazyGridState = rememberLazyGridState()




    // 화면이 처음 구성될 때 프로필 데이터를 로드합니다.
    LaunchedEffect(Unit) {
        viewModel.loadProfile(memberId)
        viewModel.loadTracks(memberId, page = currentPage, size = 9)
    }

    // 스크롤 상태를 감시해서 마지막 아이템이 보이면 다음 페이지를 로드
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .map { it ?: 0 }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex >= tracks.size - 1 && !isLoading) {
                    isLoading = true
                    currentPage++
                    viewModel.loadTracks(memberId, page = currentPage, size = 9)
                    isLoading = false
                }
            }
    }

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
                    followerCount = 1230, // 예시 데이터
                    followingCount = 4560, // 예시 데이터
                    trackCount = tracks.size,
                    showFollowButton = memberId != currentUserId
                )
            }
            // 트랙 아이템들 렌더링
            items(count = tracks.size) { index ->
                val track = tracks[index]
                TrackGridItem(track = track, onClick = { /* 트랙 클릭 처리 */ })
            }
            // 로딩 인디케이터 (옵션)
            if (isLoading) {
                item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                    Text("Loading...", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@Composable
fun TrackGridItem(
    track: ProfileResponse.GetMemberTracksResponse,
    onClick: () -> Unit
) {
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
        }
        Text(
            text = track.title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1
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
    showFollowButton: Boolean = false
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
                        .clip(RoundedCornerShape(32.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Image",
                        tint = Color.LightGray                        ,
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
            Button(onClick = { /* 팔로우 처리 */ }) {
                Text(text = "팔로우")
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
                statValue = formatNumber(followerCount)
            )
            ProfileStat(
                statLabel = "Following",
                statValue = formatNumber(followingCount)
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
    statValue: String
) {
    val customColors = CustomColors()
    Box(
        modifier = Modifier.padding(8.dp),
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
