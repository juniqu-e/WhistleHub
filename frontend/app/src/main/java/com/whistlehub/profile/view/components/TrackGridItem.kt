package com.whistlehub.profile.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.whistlehub.common.data.remote.dto.response.ProfileResponse
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography

/**
 * 트랙 그리드 아이템을 표시하는 컴포넌트
 *
 * @param track 표시할 트랙 데이터
 * @param onClick 아이템 클릭 시 실행할 콜백
 * @param onLongClick 아이템 길게 클릭 시 실행할 콜백
 */
@Composable
fun TrackGridItem(
    track: ProfileResponse.GetMemberTracksResponse,
    modifier: Modifier
) {
    val customColors = CustomColors()

    Column(
        modifier = Modifier
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
            style = Typography.bodyMedium,
            maxLines = 1,
            color = customColors.Grey200,
            overflow = TextOverflow.Ellipsis
        )
    }
}