package com.whistlehub.common.view.track

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.whistlehub.R
import com.whistlehub.common.data.remote.dto.response.TrackResponse
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.playlist.data.TrackEssential
import com.whistlehub.playlist.viewmodel.TrackPlayViewModel
import com.whistlehub.profile.view.components.ProfileTrackDetailSheet
import com.whistlehub.workstation.viewmodel.WorkStationViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TrackItemRow(
    track: TrackEssential,
    style: TrackItemStyle = TrackItemStyle.DEFAULT,
    rank: Int = 0,
    trackPlayViewModel: TrackPlayViewModel = hiltViewModel(),
    workStationViewModel: WorkStationViewModel,
    navController: NavHostController,
    needMoreView: Boolean = false,
) {
    val coroutineScope = rememberCoroutineScope()
    val currentTrack by trackPlayViewModel.currentTrack.collectAsState(initial = null)
    val isPlaying by trackPlayViewModel.isPlaying.collectAsState(initial = false)
    val user by trackPlayViewModel.user.collectAsState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var trackInfo by remember { mutableStateOf<TrackResponse.GetTrackDetailResponse?>(null) }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .combinedClickable(
                onClick = {
                    coroutineScope.launch {
                        trackPlayViewModel.playTrack(track.trackId)
                    }
                },
                onLongClick = {
                    coroutineScope.launch {
                        coroutineScope.launch {
                            trackInfo = trackPlayViewModel.getTrackbyTrackId(track.trackId)
                            showBottomSheet = true
                        }
                    }
                }
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = track.imageUrl,
            contentDescription = "Track Image",
            modifier = Modifier
                .padding(10.dp)
                .size(50.dp)
                .clip(RoundedCornerShape(5.dp)),
            error = painterResource(R.drawable.default_track),
            contentScale = ContentScale.Crop
        )
        // 랭킹인 경우
        if (style == TrackItemStyle.RANKING) {
            Text(
                text = "$rank",
                style = Typography.titleSmall,
                color = CustomColors().CommonTextColor,
                modifier = Modifier
                    .padding(bottom = 20.dp)
            )
        }

        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 10.dp)
        ) {
            Text(
                track.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = Typography.titleLarge,
                color = CustomColors().CommonTextColor
            )
            Text(
                track.artist,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = Typography.bodyMedium,
                color = CustomColors().CommonSubTextColor
            )
        }
        if (currentTrack?.trackId == track.trackId && isPlaying) {
            // Add current track specific UI here
            IconButton({ trackPlayViewModel.pauseTrack() }) {
                Icon(
                    Icons.Filled.Pause, contentDescription = "Pause", tint = CustomColors().CommonIconColor
                )
            }
        } else {
            IconButton({
                coroutineScope.launch {
                    trackPlayViewModel.playTrack(track.trackId)
                }
            }) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "Play",
                    tint = CustomColors().CommonIconColor
                )
            }
        }
        if (needMoreView) {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        trackInfo = trackPlayViewModel.getTrackbyTrackId(track.trackId)
                        showBottomSheet = true
                    }
                }
            ) {
                Icon(
                    Icons.Rounded.MoreVert,
                    contentDescription = "More",
                    tint = CustomColors().CommonIconColor
                )
            }
        }
        if (showBottomSheet && trackInfo != null) {
            ProfileTrackDetailSheet(
                track = trackInfo!!,
                isOwnProfile = user?.memberId == trackInfo!!.artist.memberId,
                sheetState = sheetState,
                onDismiss = { showBottomSheet = false },
                workStationViewModel = workStationViewModel,
                navController = navController
            )
        }
    }
}

enum class TrackItemStyle {
    DEFAULT,
    RANKING
}