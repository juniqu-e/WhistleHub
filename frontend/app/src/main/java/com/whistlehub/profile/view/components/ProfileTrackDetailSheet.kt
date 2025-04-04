package com.whistlehub.profile.view.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.whistlehub.common.data.remote.dto.response.TrackResponse
import com.whistlehub.common.util.uriToMultipartBodyPart
import com.whistlehub.common.view.component.ImageUpload
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.playlist.viewmodel.PlaylistViewModel
import com.whistlehub.profile.viewmodel.ProfileTrackDetailViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileTrackDetailSheet(
    track: TrackResponse.GetTrackDetailResponse,
    isOwnProfile: Boolean,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    viewModel: ProfileTrackDetailViewModel = hiltViewModel(),
    playlistViewModel: PlaylistViewModel = hiltViewModel()
) {
    val customColors = CustomColors()
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // States
    val isLiked by viewModel.isLiked.collectAsState()
    val likeCount by viewModel.likeCount.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isUpdateLoading by viewModel.isUpdateLoading.collectAsState(initial = false)
    val updateSuccess by viewModel.updateSuccess.collectAsState(initial = false)
    val isDeleteLoading by viewModel.isDeleteLoading.collectAsState(initial = false)
    val deleteSuccess by viewModel.deleteSuccess.collectAsState(initial = false)

    // Track Edit Dialog state
    var showEditDialog by remember { mutableStateOf(false) }
    var editedTitle by remember { mutableStateOf(track.title) }
    var editedDescription by remember { mutableStateOf(track.description ?: "") }
    var editedVisibility by remember { mutableStateOf(true) } // Default to public

    // Track Delete Confirmation Dialog state
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Report Dialog state
    var showReportDialog by remember { mutableStateOf(false) }

    // Add to Playlist Dialog state
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var userPlaylists by remember { mutableStateOf<List<com.whistlehub.common.data.remote.dto.response.PlaylistResponse.GetMemberPlaylistsResponse>>(emptyList()) }

    // Create Playlist Dialog state
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var newPlaylistDescription by remember { mutableStateOf("") }

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
            // Handle update success
            viewModel.resetUpdateStatus()
            showEditDialog = false
        }
    }

    LaunchedEffect(deleteSuccess) {
        if (deleteSuccess) {
            // Handle delete success
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(scrollState)
        ) {
            // Track Information
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Track Image
                AsyncImage(
                    model = track.imageUrl,
                    contentDescription = track.title,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

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
                        color = customColors.Mint500,
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
                title = "Add to Playlist",
                onClick = { showAddToPlaylistDialog = true }
            )

            ActionItem(
                icon = Icons.Default.Download,
                title = "Import to My Track",
                onClick = { /* Handle import action */ }
            )

            // Actions specific to own profile
            if (isOwnProfile) {
                ActionItem(
                    icon = Icons.Default.Edit,
                    title = "Edit Track Info",
                    onClick = { showEditDialog = true }
                )

                ActionItem(
                    icon = Icons.Default.Delete,
                    title = "Delete Track",
                    textColor = customColors.Error700,
                    onClick = { showDeleteConfirmDialog = true }
                )
            } else {
                // Actions specific to other user's profile
                ActionItem(
                    icon = Icons.Default.Report,
                    title = "Report",
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

    // Edit Track Dialog
    if (showEditDialog) {
        var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
        val context = LocalContext.current

        Dialog(onDismissRequest = { showEditDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = customColors.Grey900
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    // Header
                    Text(
                        "Edit Track",
                        style = Typography.titleLarge,
                        color = customColors.Grey50,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Content
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Use the existing ImageUpload composable
                        ImageUpload(
                            onChangeImage = { uri ->
                                selectedImageUri = uri
                            },
                            originImageUri = null,
                            canDelete = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Title field
                        OutlinedTextField(
                            value = editedTitle,
                            onValueChange = { editedTitle = it },
                            label = { Text("Title", color = customColors.Grey300) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = customColors.Mint500,
                                unfocusedBorderColor = customColors.Grey700,
                                focusedTextColor = customColors.Grey50,
                                unfocusedTextColor = customColors.Grey50
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Next
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Description field
                        OutlinedTextField(
                            value = editedDescription,
                            onValueChange = { editedDescription = it },
                            label = { Text("Description", color = customColors.Grey300) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = customColors.Mint500,
                                unfocusedBorderColor = customColors.Grey700,
                                focusedTextColor = customColors.Grey50,
                                unfocusedTextColor = customColors.Grey50
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Visibility toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Visibility: ",
                                style = Typography.bodyLarge,
                                color = customColors.Grey200
                            )
                            Switch(
                                checked = editedVisibility,
                                onCheckedChange = { editedVisibility = it },
                                thumbContent = {
                                    Icon(
                                        imageVector = if (editedVisibility) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = customColors.Mint500,
                                    checkedTrackColor = customColors.Mint900,
                                    uncheckedThumbColor = customColors.Grey700,
                                    uncheckedTrackColor = customColors.Grey900,
                                )
                            )
                            Text(
                                text = if (editedVisibility) "Public" else "Private",
                                style = Typography.bodyMedium,
                                color = customColors.Grey300,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { showEditDialog = false },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = customColors.Grey300
                                )
                            ) {
                                Text("Cancel")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val imageMultipart = selectedImageUri?.let { uri ->
                                            // Use the ImageUploader utility to convert Uri to MultipartBody.Part
                                            uriToMultipartBodyPart(context, uri)
                                        }

                                        // Update the track info with optional image
                                        viewModel.updateTrackInfo(
                                            trackId = track.trackId,
                                            title = editedTitle,
                                            description = editedDescription,
                                            visibility = editedVisibility,
                                            image = imageMultipart // Pass the image part if it exists
                                        )
                                    }
                                },
                                enabled = !isUpdateLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = customColors.Mint500,
                                    contentColor = customColors.Grey950
                                )
                            ) {
                                if (isUpdateLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = customColors.Grey950,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Save")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Track", color = customColors.Error700) },
            text = {
                Text(
                    "Are you sure you want to delete this track? This action cannot be undone.",
                    color = customColors.Grey200
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.deleteTrack(track.trackId)
                        }
                    },
                    enabled = !isDeleteLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = customColors.Error700,
                        contentColor = customColors.Grey50
                    )
                ) {
                    if (isDeleteLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = customColors.Grey50,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Delete")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = customColors.Grey300
                    )
                ) {
                    Text("Cancel")
                }
            },
            containerColor = customColors.Grey900,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Report Dialog
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report Track", color = customColors.Error700) },
            text = {
                Column {
                    Text(
                        "Please select a reason for reporting this track:",
                        color = customColors.Grey200
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Simple placeholder for report options
                    OutlinedTextField(
                        value = "",
                        onValueChange = { },
                        placeholder = { Text("Enter your reason", color = customColors.Grey400) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = customColors.Mint500,
                            unfocusedBorderColor = customColors.Grey700,
                            focusedTextColor = customColors.Grey50,
                            unfocusedTextColor = customColors.Grey50
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Handle report submission
                        showReportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = customColors.Error700,
                        contentColor = customColors.Grey50
                    )
                ) {
                    Text("Submit Report")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showReportDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = customColors.Grey300
                    )
                ) {
                    Text("Cancel")
                }
            },
            containerColor = customColors.Grey900,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Add to Playlist Dialog
    if (showAddToPlaylistDialog) {
        Dialog(onDismissRequest = { showAddToPlaylistDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = customColors.Grey900
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    // 헤더 부분에 제목과 X 아이콘 추가
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Add to Playlist",
                            style = Typography.titleLarge,
                            color = customColors.Grey50
                        )

                        IconButton(
                            onClick = { showAddToPlaylistDialog = false },
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

                    if (userPlaylists.isEmpty()) {
                        Text(
                            text = "플레이리스트가 존재하지 않습니다.",
                            style = Typography.bodyMedium,
                            color = customColors.Grey300
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            userPlaylists.forEach { playlist ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            coroutineScope.launch {
                                                playlistViewModel.addTrackToPlaylist(
                                                    playlist.playlistId,
                                                    track.trackId
                                                )
                                                showAddToPlaylistDialog = false
                                            }
                                        }
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Create new playlist button
                    Button(
                        onClick = {
                            showAddToPlaylistDialog = false
                            showCreatePlaylistDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = customColors.Mint500,
                            contentColor = customColors.Grey950
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("플레이리스트 생성")
                    }
                }
            }
        }
    }

    // Create Playlist Dialog
    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = { Text("Create New Playlist", color = customColors.Grey50) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        label = { Text("Playlist Name", color = customColors.Grey300) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = customColors.Mint500,
                            unfocusedBorderColor = customColors.Grey700,
                            focusedTextColor = customColors.Grey50,
                            unfocusedTextColor = customColors.Grey50
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newPlaylistDescription,
                        onValueChange = { newPlaylistDescription = it },
                        label = { Text("Description (Optional)", color = customColors.Grey300) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = customColors.Mint500,
                            unfocusedBorderColor = customColors.Grey700,
                            focusedTextColor = customColors.Grey50,
                            unfocusedTextColor = customColors.Grey50
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            // Create playlist with track already added
                            playlistViewModel.createPlaylist(
                                name = newPlaylistName,
                                description = newPlaylistDescription,
                                trackIds = listOf(track.trackId)
                            )
                            showCreatePlaylistDialog = false
                        }
                    },
                    enabled = newPlaylistName.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = customColors.Mint500,
                        contentColor = customColors.Grey950
                    )
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCreatePlaylistDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = customColors.Grey300
                    )
                ) {
                    Text("Cancel")
                }
            },
            containerColor = customColors.Grey900,
            shape = RoundedCornerShape(16.dp)
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