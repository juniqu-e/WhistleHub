package com.whistlehub.common.view.copmonent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.WhistleHubTheme

@Preview(showBackground = true)
@Composable
fun Navigation() {
    WhistleHubTheme {
        NavigationBar(modifier = Modifier.fillMaxWidth().background(CustomColors().Grey700)) {
            NavigationBarItem(false, onClick = {}, icon = {
                Icon(
                    Icons.Rounded.Home,
                    contentDescription = "홈"
                )
            })
            NavigationBarItem(false, onClick = {}, icon = {
                Icon(
                    Icons.Rounded.MusicNote,
                    contentDescription = "워크스테이션"
                )
            })
            NavigationBarItem(false, onClick = {}, icon = {
                Icon(
                    Icons.AutoMirrored.Rounded.FormatListBulleted,
                    contentDescription = "플레이리스트"
                )
            })
            NavigationBarItem(false, onClick = {}, icon = {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = "검색"
                )
            })
            NavigationBarItem(false, onClick = {}, icon = {
                Icon(
                    Icons.Rounded.Person,
                    contentDescription = "마이페이지"
                )
            })
        }
    }
}