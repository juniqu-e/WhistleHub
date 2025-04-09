package com.whistlehub.profile.view.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.whistlehub.common.data.remote.dto.response.PlaylistResponse
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography

/**
 * Dialog for adding a track to a playlist
 *
 * @param playlists List of user's playlists
 * @param onDismiss Called when user dismisses the dialog
 * @param onPlaylistSelect Called with playlist ID when user selects a playlist
 * @param onCreatePlaylistClick Called when user wants to create a new playlist
 */
@Composable
fun AddToPlaylistDialog(
    playlists: List<PlaylistResponse.GetMemberPlaylistsResponse>,
    onDismiss: () -> Unit,
    onPlaylistSelect: (Int) -> Unit,
    onCreatePlaylistClick: () -> Unit
) {
    val customColors = CustomColors()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = customColors.Grey900
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                // Header with title and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "플레이리스트에 추가",
                        style = Typography.titleLarge,
                        color = customColors.Grey50
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = customColors.Grey300
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Playlist list or empty state
                if (playlists.isEmpty()) {
                    Text(
                        text = "플레이리스트가 존재하지 않습니다.",
                        style = Typography.bodyMedium,
                        color = customColors.Grey300
                    )
                } else {
                    // FIXED: Box with fixed height to prevent infinite height constraints
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            playlists.forEach { playlist ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onPlaylistSelect(playlist.playlistId) }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Playlist image or placeholder
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(customColors.Grey700),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (playlist.imageUrl != null) {
                                            AsyncImage(
                                                model = playlist.imageUrl,
                                                contentDescription = playlist.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.PlaylistPlay,
                                                contentDescription = null,
                                                tint = customColors.Grey500
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = playlist.name,
                                        style = Typography.bodyLarge,
                                        color = customColors.Grey50,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Create new playlist button
                Button(
                    onClick = onCreatePlaylistClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = customColors.CommonButtonColor,
                        contentColor = customColors.CommonTextColor
                    )
                ) {
                    Text("플레이리스트 생성")
                }
            }
        }
    }
}