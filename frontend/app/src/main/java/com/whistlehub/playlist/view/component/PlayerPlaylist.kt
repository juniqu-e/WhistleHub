package com.whistlehub.playlist.view.component

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.track.TrackListColumn
import com.whistlehub.playlist.viewmodel.TrackPlayViewModel

@Composable
fun PlayerPlaylist(
    modifier: Modifier = Modifier,
    trackPlayViewModel: TrackPlayViewModel = hiltViewModel(),
) {
    val trackList by trackPlayViewModel.playerTrackList.collectAsState()

    TrackListColumn(modifier.background(CustomColors().Grey950.copy(alpha = 0.7f)), trackList)
}