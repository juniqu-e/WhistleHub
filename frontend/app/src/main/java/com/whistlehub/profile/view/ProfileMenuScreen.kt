package com.whistlehub.profile.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.whistlehub.common.util.LogoutManager
import com.whistlehub.common.view.navigation.Screen
import com.whistlehub.common.view.theme.CustomColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileMenuScreen(
    navController: NavHostController,
    logoutManager: LogoutManager
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("프로필 메뉴", color = Color.White) },
                navigationIcon = {
                    // 뒤로가기 아이콘을 누르면 이전 화면으로 돌아갑니다.
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("내 활동", color = Color.White, style = MaterialTheme.typography.titleLarge)
            MenuItem(label = "내 플레이리스트", onClick = {
                navController.navigate(Screen.PlayList.route)
            })
            MenuItem(label = "내 작업중인 트랙", onClick = {
                // TODO: 내 작업중인 트랙 화면으로 이동
            })
            Spacer(modifier = Modifier.height(24.dp))
            Text("내 계정", color = Color.White, style = MaterialTheme.typography.titleLarge)
            MenuItem(label = "프로필 수정", onClick = {
                navController.navigate(Screen.ProfileChange.route)
            })
            MenuItem(label = "비밀번호 변경", onClick = {
                navController.navigate(Screen.PasswordChange.route)
            })
            MenuItem(label = "로그아웃", onClick = {
                // TODO: 로그아웃 처리
            })
            MenuItem(label = "회원 탈퇴", onClick = {
                // TODO: 회원 탈퇴 처리
            }, textColor = Color.Red)
        }
    }
}

@Composable
fun MenuItem(
    label: String,
    onClick: () -> Unit,
    textColor: Color = Color.White
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}
