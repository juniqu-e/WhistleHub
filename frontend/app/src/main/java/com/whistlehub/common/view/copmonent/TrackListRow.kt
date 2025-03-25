package com.whistlehub.common.view.copmonent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.whistlehub.common.data.remote.dto.response.TrackResponse

@Composable
fun TrackListRow(
    modifier: Modifier = Modifier,
    trackList: State<List<TrackResponse.GetTrackDetailResponse>>,
) {
    if (trackList.value.isEmpty()) {
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.Top,
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        modifier = modifier.heightIn(max = 200.dp)
    ) {
        items(3) {
            TrackItemColumn(trackList.value[it])
        }
    }
}