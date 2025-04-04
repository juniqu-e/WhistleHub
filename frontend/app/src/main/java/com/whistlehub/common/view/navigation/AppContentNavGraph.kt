package com.whistlehub.common.view.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.whistlehub.common.util.LogoutManager
import com.whistlehub.common.view.home.HomeScreen
import com.whistlehub.common.view.login.LoginScreen
import com.whistlehub.playlist.view.FullPlayerScreen
import com.whistlehub.playlist.view.PlayListScreen
import com.whistlehub.playlist.view.PlaylistEditScreen
import com.whistlehub.playlist.view.PlaylistTrackListScreen
import com.whistlehub.playlist.viewmodel.TrackPlayViewModel
import com.whistlehub.profile.view.PasswordChangeScreen
import com.whistlehub.profile.view.ProfileChangeScreen
import com.whistlehub.profile.view.ProfileMenuScreen
import com.whistlehub.profile.view.ProfileScreen
import com.whistlehub.search.view.SearchScreen
import com.whistlehub.workstation.view.WorkStationScreen
import com.whistlehub.workstation.viewmodel.WorkStationViewModel
import kotlin.math.log

/**
 * 메인 앱 화면 간의 네비게이션을 처리하는 콘텐츠 네비게이션 그래프
 */
@Composable
fun AppContentNavGraph(
    navController: NavHostController,
    logoutManager: LogoutManager,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
    workStationViewModel: WorkStationViewModel,
) {
    val trackPlayViewModel = hiltViewModel<TrackPlayViewModel>()
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                paddingValues,
                trackPlayViewModel = trackPlayViewModel,
                navController = navController,
                logoutManager = logoutManager
            )
        }
        composable(route = Screen.Search.route) {
            SearchScreen(
                paddingValues,
                navController,
                trackPlayViewModel
            )
        }
        composable(route = Screen.DAW.route) {
            WorkStationScreen(
                navController = navController,
                viewModel = workStationViewModel,
                paddingValues = paddingValues
            )
//            DAWScreen()
        }
        composable(route = Screen.PlayList.route) {
            PlayListScreen(
                paddingValues,
                navController
            )
        }
        composable(route = Screen.Profile.route + "/{memberId}") { backStackEntry ->
            val memberId = backStackEntry.arguments?.getString("memberId")
            ProfileScreen(
                memberId?.toInt() ?: -1,
                paddingValues = paddingValues,
                navController = navController,
                logoutManager = logoutManager,
            )
        }
        // 프로필 메뉴 화면으로 이동
        composable(route = Screen.ProfileMenu.route) {
            ProfileMenuScreen(navController = navController, logoutManager = logoutManager)
        }
        // 프로필 수정 화면으로 이동
        composable(route = Screen.ProfileChange.route) {
            ProfileChangeScreen(navController = navController, logoutManager = logoutManager)
        }
        // 비밀번호 변경 화면으로 이동
        composable(route = Screen.PasswordChange.route) {
            PasswordChangeScreen(navController = navController, logoutManager = logoutManager)
        }
        composable(route = Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        // 플레이어 화면
        composable(route = Screen.Player.route) {
            FullPlayerScreen(
                navController = navController,
                paddingValues = paddingValues,
                trackPlayViewModel = trackPlayViewModel
            )
        }
        // 플레이리스트 트랙리스트 화면
        composable(route = Screen.PlayListTrackList.route + "/{playlistId}") { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId")
            if (playlistId != null) {
                PlaylistTrackListScreen(
                    paddingValues,
                    playlistId,
                    navController,
                    trackPlayViewModel
                )
            }
        }
        // 플레이리스트 편집 화면
        composable(route = Screen.PlayListEdit.route + "/{playlistId}") { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId")
            if (playlistId != null) {
                PlaylistEditScreen(
                    paddingValues,
                    playlistId.toInt(),
                    navController
                )
            }
        }
    }
}