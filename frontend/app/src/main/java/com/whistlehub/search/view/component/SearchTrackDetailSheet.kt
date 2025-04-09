package com.whistlehub.search.view.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.whistlehub.common.data.remote.dto.request.WorkstationRequest
import com.whistlehub.common.data.remote.dto.response.PlaylistResponse
import com.whistlehub.common.data.remote.dto.response.TrackResponse
import com.whistlehub.common.view.navigation.Screen
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.profile.view.components.ActionItem
import com.whistlehub.profile.view.components.formatDuration
import com.whistlehub.search.viewmodel.SearchViewModel
import com.whistlehub.workstation.viewmodel.WorkStationViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchTrackDetailSheet(
    track: TrackResponse.GetTrackDetailResponse,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    workStationViewModel: WorkStationViewModel,
    navController: NavHostController
) {
    val customColors = CustomColors()
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    //Add to PlayList
    var showAddToPlayListDialog by remember { mutableStateOf(false) }
    var userPlayList by remember {
        mutableStateOf<List<PlaylistResponse.GetMemberPlaylistsResponse>>(
            emptyList()
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = customColors.CommonSubBackgroundColor,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(scrollState)
        ) {
            //Track Information
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
                //Track Details
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
                        text = track.artist.nickname.takeIf { it.isNotBlank() } ?: "Unknown Artist",
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

            HorizontalDivider(color = customColors.Grey800)

            ActionItem(
                icon = Icons.Default.Download,
                title = "Import to My Track",
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
        }
    }
}