package com.whistlehub.profile.view.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.whistlehub.common.data.remote.dto.response.ProfileResponse
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.profile.view.FollowListType

/**
 * 팔로워/팔로잉 목록을 모달 바텀 시트로 표시하는 컴포넌트
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileFollowSheet(
    followListType: FollowListType,
    followers: List<ProfileResponse.GetFollowersResponse>,
    followings: List<ProfileResponse.GetFollowingsResponse>,
    hasMoreFollowers: Boolean,
    hasMoreFollowing: Boolean,
    onDismiss: () -> Unit,
    onUserClick: (Int) -> Unit,
    sheetState: SheetState,
    currentUserId: Int = 0
) {
    val customColors = CustomColors()
    val followerListState = rememberLazyListState()
    val followingListState = rememberLazyListState()

    // 검색 기능을 위한 상태
    var isSearchMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = customColors.Grey900,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // 헤더 영역
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 검색 모드일 때 뒤로가기 아이콘 표시
                if (isSearchMode) {
                    IconButton(onClick = {
                        isSearchMode = false
                        searchQuery = ""
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = customColors.Grey50
                        )
                    }
                }

                // 제목
                Text(
                    text = if (isSearchMode) "Search" else
                        if (followListType == FollowListType.FOLLOWERS) "Followers" else "Following",
                    style = Typography.titleLarge,
                    color = customColors.Grey50,
                    modifier = Modifier.weight(1f)
                )

                // 검색 버튼
                IconButton(onClick = { isSearchMode = !isSearchMode }) {
                    Icon(
                        imageVector = if (isSearchMode) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = if (isSearchMode) "Close Search" else "Search",
                        tint = customColors.Mint500
                    )
                }
            }

            // 검색 모드일 때 검색 입력 필드 표시
            AnimatedVisibility(
                visible = isSearchMode,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                // 여기에 SearchBar 컴포넌트를 추가할 수 있습니다
                // 현재는 간단하게 처리
                Text(
                    text = "Search functionality coming soon",
                    style = Typography.bodyMedium,
                    color = customColors.Grey300,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }

            Divider(
                color = customColors.Grey800,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // 팔로워 또는 팔로잉 목록
            if (followListType == FollowListType.FOLLOWERS) {
                FollowersList(
                    followers = followers,
                    listState = followerListState,
                    hasMoreItems = hasMoreFollowers,
                    onUserClick = onUserClick,
                    currentUserId = currentUserId
                )
            } else {
                FollowingsList(
                    followings = followings,
                    listState = followingListState,
                    hasMoreItems = hasMoreFollowing,
                    onUserClick = onUserClick,
                    currentUserId = currentUserId
                )
            }
        }
    }
}

/**
 * 팔로워 목록을 표시하는 컴포넌트
 */
@Composable
fun FollowersList(
    followers: List<ProfileResponse.GetFollowersResponse>,
    listState: LazyListState,
    hasMoreItems: Boolean,
    onUserClick: (Int) -> Unit,
    currentUserId: Int = 0
) {
    val customColors = CustomColors()

    if (followers.isEmpty()) {
        NoFollowersMessage()
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier.height(400.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(followers) { follower ->
                FollowListItem(
                    profileImage = follower.profileImage,
                    nickname = follower.nickname,
                    onClick = { onUserClick(follower.memberId) },
                    isCurrentUser = follower.memberId == currentUserId
                )
            }

            if (hasMoreItems) {
                item {
                    LoadingIndicator()
                }
            }
        }
    }
}

/**
 * 팔로잉 목록을 표시하는 컴포넌트
 */
@Composable
fun FollowingsList(
    followings: List<ProfileResponse.GetFollowingsResponse>,
    listState: LazyListState,
    hasMoreItems: Boolean,
    onUserClick: (Int) -> Unit,
    currentUserId: Int = 0
) {
    val customColors = CustomColors()

    if (followings.isEmpty()) {
        NoFollowingMessage()
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier.height(400.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(followings) { following ->
                FollowListItem(
                    profileImage = following.profileImage,
                    nickname = following.nickname,
                    onClick = { onUserClick(following.memberId) },
                    isCurrentUser = following.memberId == currentUserId
                )
            }

            if (hasMoreItems) {
                item {
                    LoadingIndicator()
                }
            }
        }
    }
}

@Composable
fun NoFollowersMessage() {
    val customColors = CustomColors()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.PersonSearch,
                contentDescription = null,
                tint = customColors.Grey600,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No followers yet",
                style = Typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = customColors.Grey300
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Share your profile to get followers",
                style = Typography.bodySmall,
                color = customColors.Grey500
            )
        }
    }
}

@Composable
fun NoFollowingMessage() {
    val customColors = CustomColors()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = null,
                tint = customColors.Grey600,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Not following anyone yet",
                style = Typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = customColors.Grey300
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Follow users to see their updates",
                style = Typography.bodySmall,
                color = customColors.Grey500
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { /* TODO: 추천 사용자 목록으로 이동 */ }) {
                Text(
                    text = "Discover users",
                    color = customColors.Mint500
                )
            }
        }
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = CustomColors().Mint500,
            modifier = Modifier.size(32.dp),
            strokeWidth = 2.dp
        )
    }
}