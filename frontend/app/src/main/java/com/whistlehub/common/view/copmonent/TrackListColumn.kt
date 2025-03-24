package com.whistlehub.common.view.copmonent

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import com.whistlehub.playlist.data.Track

@Composable
fun TrackListColumn(
    modifier: Modifier = Modifier,
    trackList: State<List<Track>>,
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