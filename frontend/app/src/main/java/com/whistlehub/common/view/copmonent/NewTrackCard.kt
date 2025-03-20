package com.whistlehub.common.view.copmonent

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.playlist.data.Track

@Composable
fun NewTrackCard(track: Track) {
    Box(
        Modifier
            .heightIn(max = 200.dp)
            .widthIn(max = 300.dp)
            .background(CustomColors().Grey700, RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = track.artist.profileImage,
                    contentDescription = track.artist.nickname,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = track.artist.nickname,
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .weight(1f),
                    style = Typography.titleMedium
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
                        color = CustomColors().Grey50
                    )
                    Text(
                        text = track.artist.nickname,
                        modifier = Modifier,
                        style = Typography.bodyLarge,
                        color = CustomColors().Mint500
                    )
                }
            }
        }
        Text(
            text = "RELEASED",
            modifier = Modifier
                .padding(5.dp)
                .zIndex(1f)
                .background(CustomColors().Error500, RoundedCornerShape(10.dp))
                .align(Alignment.TopEnd)
                .padding(horizontal = 8.dp, vertical = 2.dp),
            style = Typography.bodyLarge,
            color = CustomColors().Grey700
        )
    }
}