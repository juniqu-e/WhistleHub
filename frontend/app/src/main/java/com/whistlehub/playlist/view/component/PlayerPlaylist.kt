package com.whistlehub.playlist.view.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.whistlehub.common.view.track.TrackListColumn
import com.whistlehub.playlist.viewmodel.TrackPlayViewModel
import com.whistlehub.workstation.viewmodel.WorkStationViewModel

@Composable
fun PlayerPlaylist(
    modifier: Modifier = Modifier,
    trackPlayViewModel: TrackPlayViewModel = hiltViewModel(),
    workStationViewModel: WorkStationViewModel,
    navController: NavHostController
) {
    val trackList by trackPlayViewModel.playerTrackList.collectAsState()

    TrackListColumn(modifier,
        trackList,
        workStationViewModel = workStationViewModel,
        navController = navController
        )
}