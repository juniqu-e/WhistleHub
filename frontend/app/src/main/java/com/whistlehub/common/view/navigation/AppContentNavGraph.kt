package com.whistlehub.common.view.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
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

/**
 * 메인 앱 화면 간의 네비게이션을 처리하는 콘텐츠 네비게이션 그래프
 */
@Composable
fun AppContentNavGraph(
    navController: NavHostController,
    logoutManager: LogoutManager,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
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
                navController = navController
            )
        }
        composable(route = Screen.Search.route) {
            SearchScreen(navController)
        }
        composable(route = Screen.DAW.route) {
            WorkStationScreen(navController = navController)
//            DAWScreen()
        }
        composable(route = Screen.PlayList.route) {
            PlayListScreen(navController = navController)
        }
        composable(route = Screen.Profile.route, arguments = listOf(navArgument("memberId") {
            type = NavType.IntType
            defaultValue = -1 // 기본값을 -1은 로그인한 유저의 프로필을 의미
        })) { backStackEntry ->
            // 네비게이션 인자에서 memberId 읽기
            val memberIdArg = backStackEntry.arguments?.getInt("memberId")
            ProfileScreen(
                navController = navController,
                logoutManager = logoutManager,
                // memberIdArg가 -1이면 ProfileScreen 내부에서 로그인한 유저의 memberId 사용
                memberIdParam = if (memberIdArg == -1) null else memberIdArg
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
                    playlistId.toInt(),
                    navController,
                    trackPlayViewModel = trackPlayViewModel
                )
            }
        }
        // 플레이리스트 편집 화면
        composable(route = Screen.PlayListEdit.route + "/{playlistId}") { backStackEntry ->
            val playlistId = backStackEntry.arguments?.getString("playlistId")
            if (playlistId != null) {
                PlaylistEditScreen(playlistId.toInt(), navController)
            }
        }
    }
}