package com.whistlehub.common.view.track

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import com.whistlehub.common.data.remote.dto.response.TrackResponse

@Composable
fun TrackListColumn(
    modifier: Modifier = Modifier,
    trackList: State<List<TrackResponse.GetTrackDetailResponse>>,
) {
    if (trackList.value.isEmpty()) {
        return
    }
    Column(modifier) {
        trackList.value.forEach { track ->
            TrackItemRow(track)
        }
    }
}