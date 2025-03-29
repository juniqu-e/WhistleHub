package com.whistlehub.common.view.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.whistlehub.common.view.home.HomeScreen
import com.whistlehub.playlist.view.FullPlayerScreen
import com.whistlehub.playlist.view.PlayListScreen
import com.whistlehub.playlist.view.PlaylistTrackListScreen
import com.whistlehub.profile.view.ProfileScreen
import com.whistlehub.search.view.SearchScreen
import com.whistlehub.workstation.view.WorkStationScreen

/**
 * 메인 앱 화면 간의 네비게이션을 처리하는 콘텐츠 네비게이션 그래프
 */
@Composable
fun AppContentNavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
    originNavController: NavHostController  // 로그아웃을 위해 전달받은 네비게이션 컨트롤러
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(paddingValues)
        }
        composable(route = Screen.Search.route) {
            SearchScreen()
        }
        composable(route = Screen.DAW.route) {
            WorkStationScreen(navController = navController)
//            DAWScreen()
        }
        composable(route = Screen.PlayList.route) {
            PlayListScreen(navController = navController)
        }
        composable(route = Screen.Profile.route) {
            ProfileScreen(/*navController = navController, */originNavController = originNavController)  // 로그아웃을 위해 최상위 컨트롤러 전달
        }
        // 플레이어 화면
        composable(route = Screen.Player.route) {
            FullPlayerScreen(navController = navController, paddingValues = paddingValues)
        }
        // 플레이리스트 트랙리스트 화면
        composable(route = Screen.PlayListTrackList.route + "/{playlistId}") { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId")
            if (playlistId != null) {
                PlaylistTrackListScreen(playlistId.toInt())
            }
        }
    }
}