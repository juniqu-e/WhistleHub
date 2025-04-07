package com.whistlehub.common.view.track

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.whistlehub.playlist.data.TrackEssential
import com.whistlehub.workstation.viewmodel.WorkStationViewModel

@Composable
fun TrackListRow(
    modifier: Modifier = Modifier,
    trackList: List<TrackEssential>,
    workStationViewModel: WorkStationViewModel,
    navController: NavHostController,
) {
    if (trackList.isEmpty()) {
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.Top,
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        modifier = modifier.heightIn(max = 200.dp)
    ) {
        items(3) { index ->
            if (index < trackList.size) {
                TrackItemColumn(trackList[index],
                    workStationViewModel = workStationViewModel,
                    navController = navController)
            } else {
                Box(Modifier.size(100.dp)) {
                    Text("No Data")
                }
            }
        }
    }
}