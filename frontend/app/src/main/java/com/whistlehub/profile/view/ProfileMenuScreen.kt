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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.whistlehub.common.util.LogoutManager
import com.whistlehub.common.view.component.CommonAppBar
import com.whistlehub.common.view.component.CustomAlertDialog
import com.whistlehub.common.view.navigation.Screen
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.profile.viewmodel.ProfileMenuViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileMenuScreen(
    navController: NavHostController,
    logoutManager: LogoutManager,
    viewModel: ProfileMenuViewModel = hiltViewModel()
) {
    val customColors = CustomColors()
    val coroutineScope = rememberCoroutineScope()

    // UI 상태
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

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
                    showDeleteAccountDialog = true
                },
                textColor = customColors.Error700,
                customColors = customColors
            )
        }
    }

    // 회원 탈퇴 확인 다이얼로그
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = {
                Text(
                    "회원 탈퇴",
                    style = Typography.titleLarge,
                    color = customColors.Error700
                )
            },
            text = {
                Column {
                    Text(
                        "정말로 회원 탈퇴를 진행하시겠습니까?",
                        style = Typography.bodyLarge,
                        color = customColors.Grey50
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "이 작업은 되돌릴 수 없으며, 모든 계정 데이터가 영구적으로 삭제됩니다.",
                        style = Typography.bodyMedium,
                        color = customColors.Grey300
                    )

                    if (isLoading) {
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = customColors.Error700,
                            strokeWidth = 2.dp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            val result = viewModel.deleteAccount()
                            isLoading = false

                            if (result.isSuccess) {
                                // 성공 시 로그아웃 처리 (TokenManager와 UserRepository는 TokenRefresh에서 처리됨)
                                logoutManager.emitLogout()
                                showDeleteAccountDialog = false
                            } else {
                                // 실패 시 오류 메시지 표시
                                errorMessage = result.errorMessage ?: "회원 탈퇴 처리 중 오류가 발생했습니다."
                                showDeleteAccountDialog = false
                                showErrorDialog = true
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = customColors.Error700,
                        contentColor = customColors.Grey50
                    ),
                    enabled = !isLoading
                ) {
                    Text("탈퇴하기")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAccountDialog = false },
                    enabled = !isLoading
                ) {
                    Text(
                        "취소",
                        color = customColors.Grey300
                    )
                }
            },
            containerColor = customColors.Grey800
        )
    }

    // 오류 메시지 다이얼로그
    if (showErrorDialog) {
        CustomAlertDialog(
            showDialog = showErrorDialog,
            title = "오류 발생",
            message = errorMessage,
            onDismiss = { showErrorDialog = false },
            onConfirm = { showErrorDialog = false }
        )
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