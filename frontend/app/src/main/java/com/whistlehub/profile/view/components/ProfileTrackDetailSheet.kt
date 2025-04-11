package com.whistlehub.profile.view.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.whistlehub.common.data.remote.dto.request.WorkstationRequest
import com.whistlehub.common.data.remote.dto.response.PlaylistResponse
import com.whistlehub.common.data.remote.dto.response.TrackResponse
import com.whistlehub.common.view.component.CustomAlertDialog
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.playlist.view.component.CreatePlaylistDialog
import com.whistlehub.playlist.viewmodel.PlaylistViewModel
import com.whistlehub.profile.view.dialogs.AddToPlaylistDialog
import com.whistlehub.profile.view.dialogs.DeleteTrackDialog
import com.whistlehub.profile.view.dialogs.EditTrackDialog
import com.whistlehub.profile.view.dialogs.ReportTrackDialog
import com.whistlehub.profile.viewmodel.ProfileTrackDetailViewModel
import com.whistlehub.workstation.viewmodel.WorkStationViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileTrackDetailSheet(
    track: TrackResponse.GetTrackDetailResponse,
    isOwnProfile: Boolean,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    workStationViewModel: WorkStationViewModel,
    navController: NavHostController,
    viewModel: ProfileTrackDetailViewModel = hiltViewModel(),
    playlistViewModel: PlaylistViewModel = hiltViewModel()
) {
    val customColors = CustomColors()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // States
    val isLiked by viewModel.isLiked.collectAsState()
    val likeCount by viewModel.likeCount.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isUpdateLoading by viewModel.isUpdateLoading.collectAsState(initial = false)
    val updateSuccess by viewModel.updateSuccess.collectAsState(initial = false)
    val isDeleteLoading by viewModel.isDeleteLoading.collectAsState(initial = false)
    val deleteSuccess by viewModel.deleteSuccess.collectAsState(initial = false)

    // Dialog visibility states
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    // Playlist data
    var userPlaylists by remember { mutableStateOf<List<PlaylistResponse.GetMemberPlaylistsResponse>>(emptyList()) }

    // Load initial data
    LaunchedEffect(Unit) {
        viewModel.loadTrackDetails(track.trackId)
    }

    // Load user playlists when "Add to Playlist" is clicked
    LaunchedEffect(showAddToPlaylistDialog) {
        if (showAddToPlaylistDialog) {
            playlistViewModel.getPlaylists()
            userPlaylists = playlistViewModel.playlists.value
        }
    }

    // Handle success/error states
    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            viewModel.resetUpdateStatus()
            showEditDialog = false
        }
    }

    LaunchedEffect(deleteSuccess) {
        if (deleteSuccess) {
            viewModel.resetDeleteStatus()
            onDismiss()
        }
    }

    // Main Bottom Sheet
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = customColors.Grey900,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        // FIXED: Removed verticalScroll modifier here, as ModalBottomSheet handles scrolling
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Track Information
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Track Image
                if (track.imageUrl.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
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
                } else {
                    AsyncImage(
                        model = track.imageUrl,
                        contentDescription = track.title,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                // Track Details
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = track.title,
                        style = Typography.titleLarge,
                        color = customColors.Grey50,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track.artist?.nickname ?: "Unknown Artist",
                        style = Typography.bodyLarge,
                        color = customColors.CommonSubTitleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Duration",
                            tint = customColors.Grey400,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = formatDuration(track.duration.toLong() * 1000), // Convert seconds to milliseconds
                            style = Typography.bodyMedium,
                            color = customColors.Grey400,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            // Description
            Text(
                text = "Description",
                style = Typography.titleMedium,
                color = customColors.Grey200,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
            Text(
                text = track.description ?: "No description available",
                style = Typography.bodyMedium,
                color = customColors.Grey300,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Tags Section
            if (!track.tags.isNullOrEmpty()) {
                Text(
                    text = "Tags",
                    style = Typography.titleMedium,
                    color = customColors.Grey200,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Start,
                    maxItemsInEachRow = 5 // 한 줄에 최대 태그 수
                ) {
                    track.tags.forEach { tag ->
                        Text(
                            text = "#${tag.name}",
                            style = Typography.bodySmall,
                            color = customColors.Grey200, // 중간 톤의 회색
                            modifier = Modifier.padding(end = 8.dp, bottom = 4.dp)
                        )
                    }
                }
            }

            // Stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Likes
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.clickable {
                            coroutineScope.launch {
                                viewModel.toggleLike(track.trackId)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) customColors.Error700 else customColors.Grey300,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = likeCount.toString(),
                            style = Typography.bodyLarge,
                            color = customColors.Grey200
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Likes",
                        style = Typography.bodySmall,
                        color = customColors.Grey400
                    )
                }

                // Views
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = track.viewCount.toString(),
                        style = Typography.bodyLarge,
                        color = customColors.Grey200
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Views",
                        style = Typography.bodySmall,
                        color = customColors.Grey400
                    )
                }

                // Imports
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = track.importCount.toString(),
                        style = Typography.bodyLarge,
                        color = customColors.Grey200
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Imports",
                        style = Typography.bodySmall,
                        color = customColors.Grey400
                    )
                }
            }

            Divider(color = customColors.Grey800)

            // Action Buttons
            Text(
                text = "Actions",
                style = Typography.titleMedium,
                color = customColors.Grey200,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Actions for both own and other profiles
            ActionItem(
                icon = Icons.Default.PlaylistAdd,
                title = "플레이리스트에 추가",
                onClick = { showAddToPlaylistDialog = true }
            )

            ActionItem(
                icon = Icons.Default.Download,
                title = "내 트랙으로 가져오기",
                onClick = {
                    coroutineScope.launch {
                        workStationViewModel.addLayerFromSearchTrack(
                            WorkstationRequest.ImportTrackRequest(track.trackId),
                            context = context
                        )

                        onDismiss()

                        navController.navigate("daw") {
                            popUpTo("search") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            )

            // Actions specific to own profile
            if (isOwnProfile) {
                ActionItem(
                    icon = Icons.Default.Edit,
                    title = "트랙 수정",
                    onClick = { showEditDialog = true }
                )

                ActionItem(
                    icon = Icons.Default.Delete,
                    title = "트랙 삭제",
                    textColor = customColors.Error700,
                    onClick = { showDeleteConfirmDialog = true }
                )
            } else {
                // Actions specific to other user's profile
                ActionItem(
                    icon = Icons.Default.Report,
                    title = "트랙 신고하기",
                    textColor = customColors.Error700,
                    onClick = { showReportDialog = true }
                )
            }

            // Show error message if any
            AnimatedVisibility(visible = errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    style = Typography.bodyMedium,
                    color = customColors.Error700,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Extra padding at bottom for visual comfort
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Create Playlist Dialog - FIXED: Moved this to the top of the dialogs
    if (showCreatePlaylistDialog) {
        Dialog(onDismissRequest = { showCreatePlaylistDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = customColors.Grey900
            ) {
                CreatePlaylistDialog(
                    onDismiss = { showCreatePlaylistDialog = false },
                    onCreatePlaylist = { title, description, image ->
                        coroutineScope.launch {
                            // Create playlist with track already added
                            playlistViewModel.createPlaylist(
                                name = title,
                                description = description,
                                trackIds = listOf(track.trackId),
                                image = image
                            )
                            showCreatePlaylistDialog = false
                            successMessage = "플레이리스트가 생성되었고 트랙이 추가되었습니다."
                            showSuccessDialog = true
                        }
                    }
                )
            }
        }
    }

    // Edit Track Dialog
    if (showEditDialog) {
        EditTrackDialog(
            track = track,
            isLoading = isUpdateLoading,
            onDismiss = { showEditDialog = false },
            onSave = { title, description, visibility, image ->
                coroutineScope.launch {
                    viewModel.updateTrackInfo(
                        trackId = track.trackId,
                        title = title,
                        description = description,
                        visibility = visibility,
                        image = image
                    )
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        DeleteTrackDialog(
            isLoading = isDeleteLoading,
            onDismiss = { showDeleteConfirmDialog = false },
            onConfirm = {
                coroutineScope.launch {
                    viewModel.deleteTrack(track.trackId)
                }
            }
        )
    }

    // Report Dialog
    if (showReportDialog) {
        ReportTrackDialog(
            onDismiss = { showReportDialog = false },
            onSubmit = { reason ->
                // Handle report submission
                showReportDialog = false
                // You can implement actual report submission logic here
            }
        )
    }

    // Add to Playlist Dialog
    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            playlists = userPlaylists,
            onDismiss = { showAddToPlaylistDialog = false },
            onPlaylistSelect = { playlistId ->
                coroutineScope.launch {
                    playlistViewModel.addTrackToPlaylist(playlistId, track.trackId)
                    showAddToPlaylistDialog = false
                    successMessage = "플레이리스트에 추가되었습니다."
                    showSuccessDialog = true
                }
            },
            onCreatePlaylistClick = {
                showAddToPlaylistDialog = false
                showCreatePlaylistDialog = true
            }
        )
    }

    // Success Alert Dialog
    if (showSuccessDialog) {
        CustomAlertDialog(
            showDialog = true,
            title = "성공",
            message = successMessage,
            onDismiss = {
                showSuccessDialog = false
                onDismiss() // Close the bottom sheet after confirmation
            },
            onConfirm = {
                showSuccessDialog = false
                onDismiss() // Close the bottom sheet after confirmation
            }
        )
    }
}

@Composable
fun ActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    textColor: Color = CustomColors().Grey50,
    onClick: () -> Unit
) {
    val customColors = CustomColors()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(customColors.Grey800),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = customColors.Grey300
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = Typography.titleMedium,
            color = textColor,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = customColors.Grey600
        )
    }
}

fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}