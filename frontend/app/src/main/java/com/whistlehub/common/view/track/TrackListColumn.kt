package com.whistlehub.common.view.track

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.whistlehub.playlist.data.TrackEssential

@Composable
fun TrackListColumn(
    modifier: Modifier = Modifier,
    trackList: List<TrackEssential>,
) {
    if (trackList.isEmpty()) {
        return
    }
    LazyColumn(modifier) {
        items(trackList.size) { index ->
            val track = trackList[index]
            TrackItemRow(track)
        }
    }
}