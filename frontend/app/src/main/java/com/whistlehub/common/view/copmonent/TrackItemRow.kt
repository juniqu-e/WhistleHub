package com.whistlehub.common.view.copmonent

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.whistlehub.common.data.remote.dto.response.TrackResponse
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.playlist.viewmodel.TrackPlayViewModel
import kotlinx.coroutines.launch

@Composable
fun TrackItemRow(track: TrackResponse.GetTrackDetailResponse,
                 style: TrackItemStyle = TrackItemStyle.DEFAULT,
                 trackPlayViewModel: TrackPlayViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val currentTrack by trackPlayViewModel.currentTrack.collectAsState(initial = null)
    val isPlaying by trackPlayViewModel.isPlaying.collectAsState(initial = false)

    Row(Modifier
        .clickable {
            if (currentTrack?.trackId != track.trackId) {
                trackPlayViewModel.stopTrack()
            }
            coroutineScope.launch {
                trackPlayViewModel.playTrack(track)
            }
        }
        .fillMaxWidth()
        .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = track.imageUrl,
            contentDescription = "Track Image",
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(5.dp)),
            error = null,
            contentScale = ContentScale.Crop
        )

        if (style == TrackItemStyle.RANKING) {
            // Add ranking specific UI here
            TODO()
        }

        Column(Modifier
            .weight(1f)
            .padding(horizontal = 10.dp)) {
            Text(
                track.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = Typography.titleLarge,
                color = CustomColors().Grey50
            )
            Text(
                track.artistInfo?.nickname ?: "Unknown Artist",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = Typography.bodyMedium,
                color = CustomColors().Grey200
            )
        }

        if (currentTrack?.trackId == track.trackId && isPlaying) {
            // Add current track specific UI here
            IconButton({trackPlayViewModel.pauseTrack()}) {
                Icon(
                    Icons.Filled.Pause, contentDescription = "Pause", tint = CustomColors().Mint500
                )
            }
        } else {
            IconButton({
                coroutineScope.launch {
                    trackPlayViewModel.playTrack(track)
                }
            }) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "Play",
                    tint = CustomColors().Grey50
                )
            }
        }
    }
}

enum class TrackItemStyle {
    DEFAULT,
    RANKING
}