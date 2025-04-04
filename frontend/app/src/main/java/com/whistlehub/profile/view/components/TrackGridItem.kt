package com.whistlehub.profile.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
 */
@Composable
fun TrackGridItem(
    track: ProfileResponse.GetMemberTracksResponse,
) {
    val customColors = CustomColors()

    Column(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 트랙 이미지
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

        // 이미지와 텍스트 사이 간격 추가
        Spacer(modifier = Modifier.height(8.dp))

        // 트랙 제목
        Text(
            text = track.title,
            style = Typography.bodyMedium,
            maxLines = 1,
            color = customColors.Grey200,
            overflow = TextOverflow.Ellipsis
        )
    }
}