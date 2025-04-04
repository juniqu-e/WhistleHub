package com.whistlehub.common.view.track

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.whistlehub.R
import com.whistlehub.common.data.remote.dto.response.TrackResponse
import com.whistlehub.common.view.navigation.Screen
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Pretendard
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.playlist.viewmodel.TrackPlayViewModel
import kotlinx.coroutines.launch

@Composable
fun NewTrackCard(
    track: TrackResponse.GetTrackDetailResponse,
    trackPlayViewModel: TrackPlayViewModel,
    navController: NavHostController
) {
    val coroutineScope = rememberCoroutineScope()

    Box(
        Modifier
            .height(180.dp)
            .aspectRatio(1.618f)
            .padding(8.dp)
            .clickable {
                coroutineScope.launch {
                    trackPlayViewModel.playTrack(track.trackId)
                }
            }
    ) {
        AsyncImage(
            model = track.imageUrl,
            contentDescription = track.imageUrl,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp)),
            error = painterResource(R.drawable.default_track),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CustomColors().Grey900.copy(alpha = 0.5f))
                .clip(RoundedCornerShape(10.dp))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(Modifier
                .clickable {
                navController.navigate(Screen.Profile.route + "/${track.artist.memberId}")
            }, verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = track.artist.profileImage ?: R.drawable.default_profile,
                    contentDescription = track.artist.nickname,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    error = painterResource(R.drawable.default_profile),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = track.artist.nickname,
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
            Row(Modifier.weight(1f),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = track.title,
                        modifier = Modifier,
                        style = Typography.headlineMedium,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Bold,
                        color = CustomColors().Grey50,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track.description ?: "",
                        modifier = Modifier,
                        style = Typography.bodyLarge,
                        color = CustomColors().Grey400,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}