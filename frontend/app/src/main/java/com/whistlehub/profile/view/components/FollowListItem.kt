package com.whistlehub.profile.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography

/**
 * 팔로워/팔로잉 목록의 각 아이템을 표시하는 컴포넌트
 *
 * @param profileImage 사용자 프로필 이미지 URL
 * @param nickname 사용자 닉네임
 * @param onClick 아이템 클릭 시 실행할 콜백
 * @param isCurrentUser 현재 로그인한 사용자인지 여부
 */
@Composable
fun FollowListItem(
    profileImage: String?,
    nickname: String,
    onClick: () -> Unit,
    isCurrentUser: Boolean = false
) {
    val customColors = CustomColors()

    // 행 전체가 클릭 가능하도록 수정
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isCurrentUser) customColors.Mint950.copy(alpha = 0.3f)
                else customColors.Grey900.copy(alpha = 0.5f)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 프로필 이미지
        if (!profileImage.isNullOrEmpty()) {
            AsyncImage(
                model = profileImage,
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(customColors.Grey800)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
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

        Spacer(modifier = Modifier.width(16.dp))

        // 사용자 정보 영역
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = nickname,
                style = Typography.bodyLarge,
                color = if (isCurrentUser) customColors.Mint300 else customColors.Grey50,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (isCurrentUser) {
                Text(
                    text = "You",
                    style = Typography.bodySmall,
                    color = customColors.Mint500
                )
            }
        }
    }
}