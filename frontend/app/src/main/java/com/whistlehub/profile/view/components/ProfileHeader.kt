package com.whistlehub.profile.view.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography

/**
 * 프로필 헤더를 표시하는 컴포넌트
 * 프로필 이미지, 닉네임, 자기소개, 팔로우 버튼, 통계 등을 포함합니다.
 */
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
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(colors.Grey700),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Image",
                        tint = colors.Grey300,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = nickname,
                        style = Typography.titleLarge,
                        color = colors.Grey50,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // 본인이 아닌 경우에만 팔로우 버튼 표시
                    if (showFollowButton) {
                        Button(
                            onClick = {
                                Log.d(
                                    "ProfileHeader",
                                    "Follow button clicked, current state: $isFollowing"
                                )
                                onFollowClick()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowing) colors.Error700 else colors.CommonButtonColor,
                                contentColor = colors.CommonTextColor
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(
                                horizontal = 8.dp,
                                vertical = 4.dp
                            )
                        ) {
                            // Show different text and icon based on follow state
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (isFollowing) {
                                    Icon(
                                        imageVector = Icons.Default.PersonRemove,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = colors.CommonTextColor
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.PersonAdd,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = colors.CommonTextColor
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    text = profileText,
                    style = Typography.bodyMedium,
                    maxLines = 2,
                    color = colors.Grey50
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