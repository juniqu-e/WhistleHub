package com.whistlehub.common.view.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "메인", Icons.Default.Home)
    data object Search : Screen("search", "검색", Icons.Default.Search)
    data object DAW : Screen("daw", "작업실", Icons.Default.MusicNote)
    data object PlayList : Screen("playlist", "내 목록", Icons.AutoMirrored.Filled.List)
    data object Profile : Screen("profile", "프로필", Icons.Default.AccountCircle)
    data object Player : Screen("player", "플레이어", Icons.Default.MusicNote)
    data object PlayListTrackList : Screen("playlist_track_list", "플레이리스트 트랙리스트", Icons.Default.MusicNote)
}
