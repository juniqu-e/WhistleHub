package com.whistlehub.profile.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography

/**
 * 프로필 통계(트랙, 팔로워, 팔로잉) 아이템을 표시하는 컴포넌트
 *
 * @param statLabel 통계 라벨 (예: "Tracks", "Followers", "Following")
 * @param statValue 통계 값
 * @param onClick 아이템 클릭 시 실행할 콜백
 */
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
                style = Typography.titleLarge,
                color = customColors.Grey50
            )
            Text(
                text = statLabel,
                style = Typography.bodyMedium,
                color = customColors.Grey50
            )
        }
    }
}

/**
 * 숫자 포맷팅 함수 (예: 1000 -> 1k+)
 */
fun formatNumber(number: Int): String {
    return when {
        number >= 1_000_000_000 -> "${number / 1_000_000_000}b+"
        number >= 1_000_000 -> "${number / 1_000_000}m+"
        number >= 1_000 -> "${number / 1_000}k+"
        else -> number.toString()
    }
}