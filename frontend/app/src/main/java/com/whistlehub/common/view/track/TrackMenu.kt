package com.whistlehub.common.view.track

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.whistlehub.R
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.playlist.viewmodel.TrackPlayViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrackMenu(
    trackPlayViewModel: TrackPlayViewModel = hiltViewModel(),
    onReportClick: () -> Unit = {},
    onAddToPlaylistClick: () -> Unit = {},
    onImportToMyTrackClick: () -> Unit = {}
) {
    val currentTrack by trackPlayViewModel.currentTrack.collectAsState(initial = null)
    Column(modifier = Modifier.heightIn(min = 200.dp).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Bottom),
        horizontalAlignment = Alignment.CenterHorizontally) {
        if (currentTrack?.imageUrl != null) {
            AsyncImage(
                model = currentTrack!!.imageUrl,
                contentDescription = "Track Image",
                modifier = Modifier.size(75.dp),
                contentScale = ContentScale.Crop,
            )
        } else {
            // 기본 배경 이미지
            Image(
                painterResource(R.drawable.default_track),
                contentDescription = "Track Image",
                modifier = Modifier.size(75.dp),
                contentScale = ContentScale.Crop)
        }
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = currentTrack?.title ?: "Track Title",
            style = Typography.titleMedium,
            color = CustomColors().Grey50,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = currentTrack?.artistInfo?.nickname ?: "Artist Name",
            style = Typography.bodyLarge,
            color = CustomColors().Mint500,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "Tags",
            style = Typography.titleSmall,
            color = CustomColors().Grey200,
            textAlign = TextAlign.Center
        )
        if (currentTrack?.tags?.isNotEmpty() == true) {
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)) {
                currentTrack?.tags?.forEach { tag ->
                    Button({}) {
                        Text(
                            text = tag.name,
                            style = Typography.bodySmall,
                            color = CustomColors().Grey950,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "태그가 없습니다.",
                style = Typography.bodySmall,
                color = CustomColors().Grey200,
                textAlign = TextAlign.Center
            )
        }
        Row(
            Modifier.clickable{
                onAddToPlaylistClick()
            }.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("플레이리스트에 추가")
            IconButton({}) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForwardIos,
                    contentDescription = "플레이리스트에 추가",
                    tint = CustomColors().Grey200,
                    modifier = Modifier.size(16.dp))
            }
        }
        HorizontalDivider(thickness = 1.dp, color = CustomColors().Grey50)
        Row(
            Modifier.clickable{
                onAddToPlaylistClick()
            }.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("내 트랙에 Import")
            IconButton({}) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForwardIos,
                    contentDescription = "내 트랙에 Import",
                    tint = CustomColors().Grey200,
                    modifier = Modifier.size(16.dp))
            }
        }
        if (true /* 내 트랙이 아닐 때 */) {
            HorizontalDivider(thickness = 1.dp, color = CustomColors().Grey50)
            Row(
                Modifier.clickable {
                    onReportClick()
                }.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("신고")
                IconButton({
                    onReportClick()
                }) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowForwardIos,
                        contentDescription = "신고",
                        tint = CustomColors().Grey200,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}