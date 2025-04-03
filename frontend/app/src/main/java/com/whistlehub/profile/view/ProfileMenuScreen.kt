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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.whistlehub.common.util.LogoutManager
import com.whistlehub.common.view.component.CommonAppBar
import com.whistlehub.common.view.navigation.Screen
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import kotlinx.coroutines.launch

@Composable
fun ProfileMenuScreen(
    navController: NavHostController,
    logoutManager: LogoutManager
) {
    val customColors = CustomColors()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CommonAppBar(
                title = "Profile Menu",
                navController = navController,
                logoutManager = logoutManager,
                coroutineScope = coroutineScope,
                showBackButton = true,
                showMenuButton = false,
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "내 활동",
                color = customColors.Grey50,
                style = Typography.titleLarge
            )

            MenuItem(
                label = "내 플레이리스트",
                onClick = {
                    navController.navigate(Screen.PlayList.route)
                },
                customColors = customColors
            )

            MenuItem(
                label = "내 작업중인 트랙",
                onClick = {
                    // TODO: 내 작업중인 트랙 화면으로 이동
                },
                customColors = customColors
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "내 계정",
                color = customColors.Grey50,
                style = Typography.titleLarge
            )

            MenuItem(
                label = "프로필 수정",
                onClick = {
                    navController.navigate(Screen.ProfileChange.route)
                },
                customColors = customColors
            )

            MenuItem(
                label = "비밀번호 변경",
                onClick = {
                    navController.navigate(Screen.PasswordChange.route)
                },
                customColors = customColors
            )

            MenuItem(
                label = "로그아웃",
                onClick = {
                    // 로그아웃 처리
                    coroutineScope.launch {
                        logoutManager.emitLogout()
                    }
                },
                customColors = customColors
            )

            MenuItem(
                label = "회원 탈퇴",
                onClick = {
                    // TODO: 회원 탈퇴 처리
                },
                textColor = customColors.Error700,
                customColors = customColors
            )
        }
    }
}

@Composable
fun MenuItem(
    label: String,
    onClick: () -> Unit,
    customColors: CustomColors,
    textColor: androidx.compose.ui.graphics.Color = customColors.Grey50
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
            style = Typography.bodyLarge
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}