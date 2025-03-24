package com.whistlehub.workstation.view

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.whistlehub.workstation.viewmodel.WorkStationViewModel


@Composable
fun WorkStationScreen(navController: NavController) {
    val activity = LocalActivity.current as? Activity
    val viewModel: WorkStationViewModel = hiltViewModel()
    //Like React - useEffect
    /*
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
     */
    Box(
        modifier =
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.displayCutout)
            .background(Color.LightGray)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Special Full Screen View in Landscape")
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            viewModel.bottomBarProvider.WorkStationBottomBar()
        }
    }


}