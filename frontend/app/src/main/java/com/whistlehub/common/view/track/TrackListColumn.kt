package com.whistlehub.common.view.track

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.whistlehub.playlist.data.TrackEssential
import com.whistlehub.workstation.viewmodel.WorkStationViewModel

@Composable
fun TrackListColumn(
    modifier: Modifier = Modifier,
    trackList: List<TrackEssential>,
    workStationViewModel: WorkStationViewModel,
    navController: NavHostController
) {
    if (trackList.isEmpty()) {
        return
    }
    LazyColumn(modifier) {
        items(trackList.size) { index ->
            val track = trackList[index]
            TrackItemRow(
                track,
                workStationViewModel = workStationViewModel,
                navController = navController
            )
        }
    }
}