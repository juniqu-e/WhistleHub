package com.whistlehub.common.view.track

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.whistlehub.R
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.playlist.data.TrackEssential
import com.whistlehub.playlist.viewmodel.TrackPlayViewModel
import kotlinx.coroutines.launch

@Composable
fun TrackItemRow(
    track: TrackEssential,
    style: TrackItemStyle = TrackItemStyle.DEFAULT,
    rank: Int = 0,
    trackPlayViewModel: TrackPlayViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val currentTrack by trackPlayViewModel.currentTrack.collectAsState(initial = null)
    val isPlaying by trackPlayViewModel.isPlaying.collectAsState(initial = false)

    Row(
        Modifier
            .clickable {
                if (currentTrack?.trackId != track.trackId) {
                    trackPlayViewModel.stopTrack()
                }
                coroutineScope.launch {
                    trackPlayViewModel.playTrack(track.trackId)
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
                color = CustomColors().Grey50,
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
                color = CustomColors().Grey50
            )
            Text(
                track.artist,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = Typography.bodyMedium,
                color = CustomColors().Grey200
            )
        }

        if (currentTrack?.trackId == track.trackId && isPlaying) {
            // Add current track specific UI here
            IconButton({ trackPlayViewModel.pauseTrack() }) {
                Icon(
                    Icons.Filled.Pause, contentDescription = "Pause", tint = CustomColors().Mint500
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