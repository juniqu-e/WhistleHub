package com.whistlehub.common.view.copmonent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun TrackItemColumn(
    track: TrackResponse.GetTrackDetailResponse,
    trackPlayViewModel: TrackPlayViewModel = hiltViewModel()
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable {
                trackPlayViewModel.playTrack(track)
            }, verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.Start
    ) {
        AsyncImage(
            model = track.imageUrl,
            contentDescription = "Track Image",
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(5.dp)),
            error = null,
            contentScale = ContentScale.Crop
        )
        Text(
            text = track.title,
            style = Typography.titleMedium,
            color = CustomColors().Grey50,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
        )
        Text(
            text = track.artistInfo?.nickname ?: "Unknown Artist",
            style = Typography.bodyMedium,
            color = CustomColors().Mint500,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
        )
    }
}