package com.whistlehub.profile.view

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomAppBar
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.whistlehub.common.data.remote.dto.response.ProfileResponse
import com.whistlehub.common.data.remote.dto.response.TrackResponse
import com.whistlehub.common.view.copmonent.TrackItemColumn
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.common.viewmodel.LoginViewModel
import com.whistlehub.profile.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
    originNavController: NavHostController,
    userViewModel: LoginViewModel = hiltViewModel()
) {
    val customColors = CustomColors()

    val profile = viewModel.profileState
    val tracks = viewModel.trackListState // 기본값: emptyList() 이미 설정됨
    val userInfo by userViewModel.userInfo.collectAsState()
    // 화면 진입 시 데이터 로딩
    LaunchedEffect(Unit) {
        viewModel.loadProfile(userInfo?.memberId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Whistle Hub", color = customColors.Grey50) },
                actions = {
                    IconButton(onClick = { /* 메뉴 클릭 */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "메뉴", tint = customColors.Grey50)
                    }
                    IconButton(onClick = {
                        originNavController.navigate("login") {
                            popUpTo("login") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "로그아웃", tint = customColors.Grey50)
                    }
                }
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            // 에러 메시지가 있을 경우 상단에 노출 (기본 레이아웃은 계속 표시)
            if (viewModel.errorMessage != null) {
                item {
                    Text(
                        text = "Error: ${viewModel.errorMessage}",
                        color = customColors.Error500,
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    )
                }
            }

            // 헤더 영역
            item {
                ProfileHeader(
                    userNickname = profile.nickname,
                    userDescription = profile.profileText,
                    followerCount = 1230, // 예시
                    followingCount = 4560, // 예시
                    trackCount = tracks.size
                )
            }

            // 트랙 목록 렌더링 (비어있으면 빈 리스트)
            items(tracks) { track ->
                ProfileTrackItem(
                    trackId = track.trackId,
                    nickname = track.nickname,
                    title = track.title,
                    duration = track.duration,
                    imageUrl = track.imageUrl
                )
            }
        }
    }
}

@Composable
fun ProfileTrackItem(
    trackId: Int,
    nickname: String,
    title: String,
    duration: Int,
    imageUrl: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { /* 트랙 클릭 처리 */ },
        verticalAlignment = Alignment.CenterVertically
    ) {
        imageUrl?.let {
            AsyncImage(
                model = it,
                contentDescription = title,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, style = Typography.titleMedium, color = CustomColors().Grey50)
            Text(nickname, style = Typography.bodySmall, color = CustomColors().Grey400)
            Text(
                "${duration / 60}:${duration % 60}",
                style = Typography.bodySmall,
                color = CustomColors().Grey400
            )
        }
    }
}

@Composable
fun ProfileHeader(
    userNickname: String,
    userDescription: String,
    followerCount: Int,
    followingCount: Int,
    trackCount: Int = 0
) {
    val customColors = CustomColors()
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
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Image",
                    tint = customColors.Grey50,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = userNickname,
                    style = MaterialTheme.typography.titleLarge,
                    color = customColors.Grey50
                )
                Text(
                    text = userDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    color = customColors.Grey50
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
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
