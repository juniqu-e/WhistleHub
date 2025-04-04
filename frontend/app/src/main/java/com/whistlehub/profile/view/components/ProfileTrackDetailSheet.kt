package com.whistlehub.profile.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.whistlehub.common.data.remote.dto.response.TrackResponse
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.profile.viewmodel.ProfileTrackDetailViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.compose.foundation.clickable


/**
 * 트랙 상세 정보를 표시하는 바텀 시트 컴포넌트
 *
 * @param track 표시할 트랙 상세 정보
 * @param isOwnProfile 본인의 프로필인지 여부
 * @param onDismiss 바텀 시트 닫기 콜백
 * @param viewModel 트랙 상세 정보 뷰모델
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTrackDetailSheet(
    track: TrackResponse.GetTrackDetailResponse,
    isOwnProfile: Boolean,
    onDismiss: () -> Unit,
    viewModel: ProfileTrackDetailViewModel
) {
    val customColors = CustomColors()
    val isLiked by viewModel.isLiked.collectAsState()
    val likeCount by viewModel.likeCount.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = customColors.Grey900,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Track details section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Track image
                AsyncImage(
                    model = track.imageUrl,
                    contentDescription = track.title,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Track info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = track.title,
                        style = Typography.titleLarge,
                        color = customColors.Grey50,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = track.artist?.nickname ?: "Unknown Artist",
                        style = Typography.titleSmall,
                        color = customColors.Mint500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = formatDuration(track.duration),
                        style = Typography.bodySmall,
                        color = customColors.Grey300
                    )

                    Text(
                        text = formatDate(track.createdAt),
                        style = Typography.bodySmall,
                        color = customColors.Grey300
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            if (track.description.toString().isNotEmpty()) {
                Text(
                    text = track.description.toString(),
                    style = Typography.bodyMedium,
                    color = customColors.Grey50,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Tags section
            if (track.tags != null && track.tags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    track.tags.forEach { tag ->
                        Text(
                            text = "#${tag.name}",
                            style = Typography.bodySmall,
                            color = customColors.Grey200,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }

            // Like section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.toggleLike(track.trackId) }
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isLiked) "Unlike" else "Like",
                        tint = if (isLiked) customColors.Error500 else customColors.Grey300
                    )
                }

                Text(
                    text = "$likeCount",
                    style = Typography.bodyMedium,
                    color = customColors.Grey300
                )

                Spacer(modifier = Modifier.weight(1f))

                // Source tracks count
                if (track.sourceTrack != null && track.sourceTrack.isNotEmpty()) {
                    Text(
                        text = "Source Tracks: ${track.sourceTrack.size}",
                        style = Typography.bodySmall,
                        color = customColors.Grey300
                    )
                }
            }

            Divider(color = customColors.Grey700)

            // Actions section - different based on whether it's own profile or not
            if (isOwnProfile) {
                // Own profile actions
                ActionItem(
                    icon = Icons.Default.PlaylistAdd,
                    text = "Add to Playlist",
                    color = customColors.Grey50,
                    onClick = {
                        // TODO: Implement add to playlist functionality
                    }
                )

                ActionItem(
                    icon = Icons.Default.ContentCopy,
                    text = "Import to My Tracks",
                    color = customColors.Grey50,
                    onClick = {
                        // TODO: Implement import functionality
                    }
                )

                ActionItem(
                    icon = Icons.Default.Edit,
                    text = "Edit Track Info",
                    color = customColors.Grey50,
                    onClick = {
                        // TODO: Implement edit functionality
                    }
                )

                ActionItem(
                    icon = Icons.Default.Delete,
                    text = "Delete Track",
                    color = customColors.Error500,
                    onClick = {
                        // TODO: Implement delete functionality
                    }
                )
            } else {
                // Other user's profile actions
                ActionItem(
                    icon = Icons.Default.PlaylistAdd,
                    text = "Add to Playlist",
                    color = customColors.Grey50,
                    onClick = {
                        // TODO: Implement add to playlist functionality
                    }
                )

                ActionItem(
                    icon = Icons.Default.ContentCopy,
                    text = "Import to My Tracks",
                    color = customColors.Grey50,
                    onClick = {
                        // TODO: Implement import functionality
                    }
                )

                ActionItem(
                    icon = Icons.Default.Report,
                    text = "Report",
                    color = customColors.Error500,
                    onClick = {
                        // TODO: Implement report functionality
                    }
                )
            }

            // Add extra padding at the bottom
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * 트랙 상세 화면의 액션 아이템 컴포넌트
 */
@Composable
private fun ActionItem(
    icon: ImageVector,
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = color,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            style = Typography.bodyLarge,
            color = color
        )
    }
}

/**
 * 트랙 재생 시간을 포맷하는 함수
 */
private fun formatDuration(durationInSeconds: Int): String {
    val minutes = TimeUnit.SECONDS.toMinutes(durationInSeconds.toLong())
    val seconds = durationInSeconds - TimeUnit.MINUTES.toSeconds(minutes)
    return String.format("%d:%02d", minutes, seconds)
}

/**
 * 날짜 문자열을 포맷하는 함수
 */
private fun formatDate(dateString: String): String {
    try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        return if (date != null) outputFormat.format(date) else dateString
    } catch (e: Exception) {
        return dateString
    }
}