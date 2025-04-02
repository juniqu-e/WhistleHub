package com.whistlehub.common.view.track

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.whistlehub.R
import com.whistlehub.common.data.remote.dto.response.TrackResponse
import com.whistlehub.common.view.navigation.Screen
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.playlist.viewmodel.TrackPlayViewModel
import kotlinx.coroutines.launch

@Composable
fun NewTrackCard(
    track: TrackResponse.GetTrackDetailResponse,
    trackPlayViewModel: TrackPlayViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val coroutineScope = rememberCoroutineScope()

    Box(
        Modifier
            .heightIn(max = 200.dp)
            .widthIn(max = 300.dp)
            .background(CustomColors().Grey700, RoundedCornerShape(10.dp))
            .padding(8.dp)
            .clickable {
                coroutineScope.launch {
                    trackPlayViewModel.playTrack(track.trackId)
                }
            }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.clickable {
                navController.navigate(Screen.Profile.route + "/${track.artist?.memberId}")
            }, verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = track.artist?.profileImage ?: R.drawable.default_profile,
                    contentDescription = track.artist?.nickname,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = track.artist?.nickname ?: "Unknown Artist",
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .weight(1f),
                    style = Typography.titleMedium,
                    color = CustomColors().Grey50,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "RELEASED",
                    modifier = Modifier
                        .padding(5.dp)
                        .background(CustomColors().Error500, RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    style = Typography.bodyLarge,
                    color = CustomColors().Grey700
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AsyncImage(
                    model = track.imageUrl,
                    contentDescription = track.imageUrl,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    contentScale = ContentScale.Crop,
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = track.title,
                        modifier = Modifier,
                        style = Typography.titleLarge,
                        color = CustomColors().Grey50,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track.artist?.nickname ?: "Unknown Artist",
                        modifier = Modifier,
                        style = Typography.bodyLarge,
                        color = CustomColors().Mint500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}