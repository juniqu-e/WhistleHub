package com.whistlehub.common.view.track

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.sp
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
fun TrackItemColumn(
    track: TrackEssential,
    workStationViewModel: WorkStationViewModel,
    navController: NavHostController,
    trackPlayViewModel: TrackPlayViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val user by trackPlayViewModel.user.collectAsState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var trackInfo by remember { mutableStateOf<TrackResponse.GetTrackDetailResponse?>(null) }

    Column(
        Modifier
            .fillMaxWidth()
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
        verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.Top),
        horizontalAlignment = Alignment.Start
    ) {
        AsyncImage(
            model = track.imageUrl,
            contentDescription = "Track Image",
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(5.dp)),
            error = painterResource(R.drawable.default_track),
            contentScale = ContentScale.Crop
        )
        Text(
            text = track.title + "\n",
            style = Typography.titleMedium,
            lineHeight = 18.sp,
            color = CustomColors().CommonTextColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
        )
        Text(
            text = track.artist,
            style = Typography.bodyMedium,
            color = CustomColors().CommonSubTextColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
        )
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