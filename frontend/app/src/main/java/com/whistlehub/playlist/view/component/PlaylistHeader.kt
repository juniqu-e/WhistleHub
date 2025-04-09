package com.whistlehub.playlist.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.whistlehub.R
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography

/**
 * 플레이리스트 헤더 컴포넌트
 *
 * @param isLikedPlaylist 좋아요 트랙 목록인지 여부
 * @param playlistInfo 플레이리스트 정보
 * @param tracksCount 트랙 수
 * @param totalDuration 총 재생 시간
 * @param onPlayClick 재생 버튼 클릭 시 호출되는 콜백
 * @param onEditClick 편집 버튼 클릭 시 호출되는 콜백
 * @param modifier 수정자
 */
@Composable
fun PlaylistHeader(
    isLikedPlaylist: Boolean,
    playlistInfo: com.whistlehub.common.data.remote.dto.response.PlaylistResponse.GetPlaylistResponse?,
    tracksCount: Int,
    totalDuration: String,
    onPlayClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = CustomColors()

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 플레이리스트 이미지 또는 좋아요 아이콘
        if (isLikedPlaylist) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.CommonBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = "Favorite",
                    modifier = Modifier.size(100.dp),
                    tint = colors.Error700
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Liked Tracks",
                style = Typography.displaySmall,
                color = colors.Grey50
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Your Favorite Tracks",
                style = Typography.bodyMedium,
                color = colors.Grey300,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            AsyncImage(
                model = playlistInfo?.imageUrl,
                contentDescription = "Playlist Cover",
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.default_track)
            )

            Spacer(Modifier.height(16.dp))
            Text(
                playlistInfo?.name ?: "Playlist",
                style = Typography.displaySmall,
                color = colors.Grey50
            )
            Spacer(Modifier.height(8.dp))
            Text(
                playlistInfo?.description ?: "",
                style = Typography.bodyMedium,
                color = colors.Grey300,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(16.dp))

        // 버튼 영역
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
        ) {
            // 편집 버튼 (일반 플레이리스트만 표시)
            if (!isLikedPlaylist) {
                Button(
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.CommonButtonColor,
                    ),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(20.dp),
                            tint = colors.CommonTextColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "수정하기",
                            style = Typography.bodyLarge,
                            color = colors.CommonTextColor,
                        )
                    }
                }
            }

            // 전체 재생 버튼
            Button(
                onClick = onPlayClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.CommonButtonColor,
                ),
                shape = RoundedCornerShape(8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        modifier = Modifier.size(20.dp),
                        tint = colors.CommonTextColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "전체 재생",
                        style = Typography.bodyLarge,
                        color = colors.CommonTextColor,
                    )
                }
            }
        }
    }
}