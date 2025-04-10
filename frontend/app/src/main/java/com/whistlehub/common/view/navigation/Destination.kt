package com.whistlehub.common.view.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Search : Screen("search", "Discovery", Icons.Default.Search)
    data object DAW : Screen("daw", "Studio", Icons.Default.MusicNote)
    data object PlayList : Screen("playlist", "Playlist", Icons.AutoMirrored.Filled.List)
    data object Profile : Screen("profile", "Profile", Icons.Default.AccountCircle)
    data object ProfileMenu : Screen("profile_menu", "프로필 메뉴", Icons.Default.AccountCircle)
    data object ProfileChange : Screen("profile_change", "프로필 수정", Icons.Default.AccountCircle)
    data object PasswordChange : Screen("password_change", "비밀번호 변경", Icons.Default.AccountCircle)
    data object Login : Screen("login", "로그인", Icons.Default.AccountCircle)
    data object Player : Screen("player", "플레이어", Icons.Default.MusicNote)
    data object PlayListTrackList :
        Screen("playlist_track_list", "플레이리스트 트랙리스트", Icons.Default.MusicNote)

    data object PlayListEdit : Screen("playlist_edit", "플레이리스트 편집", Icons.Default.MusicNote)
    data object TagRanking : Screen("tag_ranking", "태그 랭킹", Icons.Default.Search)
}
