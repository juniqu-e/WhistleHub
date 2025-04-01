package com.whistlehub.common.view

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.whistlehub.common.view.navigation.Screen


/**
 * 앱의 기본 레이아웃 구조를 제공하는 스캐폴드
 * 화면 방향 제어 및 시스템 바 관리도 담당
 */
@Composable
fun AppScaffold(
    navController: NavHostController,
    bottomBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route
    val context = LocalContext.current
    val activity = context as? Activity
    // 화면 방향 처리
    LaunchedEffect(currentRoute) {
//        if (currentRoute == Screen.DAW.route) {
//            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
//        } else {
//            //세로 고정
//            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (currentRoute != Screen.DAW.route)
                    Modifier.systemBarsPadding() else Modifier
            ),
        bottomBar = {
            if (currentRoute != "login" && currentRoute != Screen.DAW.route) {
                bottomBar()
            }
        },
        content = content
    )
}
