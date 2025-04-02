package com.whistlehub.common.view.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
import androidx.navigation.compose.currentBackStackEntryAsState
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.playlist.view.MiniPlayerBar
import com.whistlehub.playlist.viewmodel.PlayerViewState
import com.whistlehub.playlist.viewmodel.TrackPlayViewModel
import com.whistlehub.profile.viewmodel.ProfileViewModel

/**
 * 앱의 하단 네비게이션 바 레이아웃
 * 네비게이션 항목 표시 및 미니 플레이어를 관리
 */
@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val navigationList = listOf(
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
    val currentUserId by viewModel.memberId.collectAsState()

    Column(Modifier.background(Color.Transparent)) {
        // 미니 플레이어 표시 (필요한 경우만)
        if (currentRoute != Screen.Player.route && currentTrack != null) {
            MiniPlayerBar(navController)
        }
        // 하단 네비게이션 바
        NavigationBar(
            containerColor = CustomColors().Grey950.copy(alpha = 0.95f),
        ) {
            navigationList.forEachIndexed { index, screen ->
                NavigationBarItem(
                    selected = selectedNavigationIndex.intValue == index,
                    onClick = {
                        trackPlayViewModel.setPlayerViewState(PlayerViewState.PLAYING)
                        selectedNavigationIndex.intValue = index
                        // Profile 탭일 경우 현재 사용자 ID로 이동
                        if (screen == Screen.Profile) {
                            navController.navigate("${screen.route}/$currentUserId")
                        } else {
                            navController.navigate(screen.route)
                        }
                    },
                    icon = {
                        Icon(imageVector = screen.icon, contentDescription = screen.title)
                    },
                    label = {
                        Text(
                            screen.title,
                            color = if (index == selectedNavigationIndex.intValue)
                                Color.White
                            else Color.Gray
                        )
                    }
                )
            }
        }
    }
}