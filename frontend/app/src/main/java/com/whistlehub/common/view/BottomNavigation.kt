package com.whistlehub.common.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.whistlehub.common.view.home.HomeScreen
import com.whistlehub.common.view.navigation.Screen
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.playlist.view.FullPlayerScreen
import com.whistlehub.playlist.view.MiniPlayerBar
import com.whistlehub.playlist.view.PlayListScreen
import com.whistlehub.playlist.viewmodel.PlayerViewState
import com.whistlehub.playlist.viewmodel.TrackPlayViewModel
import com.whistlehub.profile.view.ProfileScreen
import com.whistlehub.search.view.SearchScreen
import com.whistlehub.workstation.view.WorkStationScreen

@Composable
fun WhistleHubNavigation(navController: NavHostController) {
    val navigationList = listOf<Screen>(
        Screen.Home,
        Screen.Search,
        Screen.DAW,
        Screen.PlayList,
        Screen.Profile
    )
    val selectedNavigationIndex = rememberSaveable {
        mutableIntStateOf(0)
    }
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route
    val trackPlayViewModel = hiltViewModel<TrackPlayViewModel>()
    val currentTrack by trackPlayViewModel.currentTrack.collectAsState(initial = null)

    if (currentRoute != Screen.DAW.route) {
        Column(Modifier.background(Color.Transparent)) {
            if (currentRoute != Screen.Player.route && currentTrack != null) {
                MiniPlayerBar(navController)
            }
            NavigationBar(
                containerColor = CustomColors().Grey950.copy(alpha = 0.95f),
            ) {
                navigationList.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        selected = selectedNavigationIndex.intValue == index,
                        onClick = {
                            trackPlayViewModel.setPlayerViewState(PlayerViewState.PLAYING)
                            selectedNavigationIndex.intValue = index
                            navController.navigate(screen.route)
                        },
                        icon = {
                            Icon(imageVector = screen.icon, contentDescription = screen.title)
                        },
                        label = {
                            Text(
                                screen.title,
                                color = if (index == selectedNavigationIndex.intValue)
                                    Color.Black
                                else Color.Gray
                            )
                        },
                        //                colors = NavigationBarItemDefaults.colors(
                        //                    selectedIconColor = MaterialTheme.colorScheme.surface,
                        //                    indicatorColor = MaterialTheme.colorScheme.primary
                        //                )
                    )
                }
            }
        }
    }


}

@Composable
fun WhistleHubNavHost(
    navController: NavHostController, paddingValues: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route, modifier = Modifier
    ) {
        composable(route = Screen.Home.route) { HomeScreen(paddingValues) }
        composable(route = Screen.Search.route) { SearchScreen() }
        composable(route = Screen.DAW.route) { WorkStationScreen(navController = navController) }
        composable(route = Screen.PlayList.route) { PlayListScreen() }
        composable(route = Screen.Profile.route) { ProfileScreen() }

        // 기타화면
        composable(route = Screen.Player.route) {
            FullPlayerScreen(navController = navController, paddingValues = paddingValues)
        }
    }
}