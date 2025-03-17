package com.whistlehub.common.view

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.whistlehub.common.view.home.HomeScreen
import com.whistlehub.common.view.navigation.Screen
import com.whistlehub.playlist.view.PlayListScreen
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

    NavigationBar {
        navigationList.forEachIndexed { index, screen ->
            NavigationBarItem(
                selected = selectedNavigationIndex.intValue == index,
                onClick = {
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

@Composable
fun WhistleHubNavHost(
    navController: NavHostController,
    modifier: Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(route = Screen.Home.route) { HomeScreen() }
        composable(route = Screen.Search.route) { SearchScreen() }
        composable(route = Screen.DAW.route) { WorkStationScreen() }
        composable(route = Screen.PlayList.route) { PlayListScreen() }
        composable(route = Screen.Profile.route) { ProfileScreen() }
    }
}