package com.whistlehub.workstation.view

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.whistlehub.workstation.viewmodel.WorkStationViewModel



@Composable
fun WorkStationScreen(navController: NavController) {
    val activity = LocalActivity.current as? Activity
    val viewModel: WorkStationViewModel = hiltViewModel()
    //Like React - useEffect
    DisposableEffect(Unit) {
        // 원래 orientation 저장
        val originalOrientation = activity?.requestedOrientation
        // landscape로 강제 변경
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            // WorkStation 벗어날때 원래 orientation 복구
            activity?.requestedOrientation = originalOrientation
                ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }



    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Special Full Screen View in Landscape")
    }

    viewModel.bottomBarProvider.WorkStationBottomBar()
}