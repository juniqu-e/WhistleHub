package com.whistlehub.common.view.track

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import com.whistlehub.playlist.data.TrackEssential

@Composable
fun TrackListColumn(
    modifier: Modifier = Modifier,
    trackList: State<List<TrackEssential>>,
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