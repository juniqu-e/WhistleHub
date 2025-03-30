package com.whistlehub.profile.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.whistlehub.common.util.LogoutManager
import com.whistlehub.common.view.theme.CustomColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordChangeScreen(
    logoutManager: LogoutManager,
    navController: NavHostController,
    // 필요하다면 뷰모델을 주입해서 API 연동, 상태 관리
    // viewModel: ChangePasswordViewModel = hiltViewModel()
) {
    val customColors = CustomColors()

    // UI 상태(예시). 실제로는 뷰모델 상태를 collectAsState()로 가져올 수 있음
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var newPasswordConfirm by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") } // 예: "새 비밀번호가 일치하지 않습니다." 등

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("비밀번호 변경", color = customColors.Grey50) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = customColors.Grey50
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1) 현재 비밀번호
            Text(
                text = "현재 Password",
                style = MaterialTheme.typography.titleMedium,
                color = customColors.Grey50
            )
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                placeholder = { Text("현재 비밀번호를 입력하세요") }
            )

            // 2) 새로운 비밀번호
            Text(
                text = "새로운 Password",
                style = MaterialTheme.typography.titleMedium,
                color = customColors.Grey50
            )
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                placeholder = { Text("새로운 비밀번호를 입력하세요") }
            )

            // 3) 새로운 비밀번호 확인
            Text(
                text = "새로운 Password Confirm",
                style = MaterialTheme.typography.titleMedium,
                color = customColors.Grey50
            )
            OutlinedTextField(
                value = newPasswordConfirm,
                onValueChange = { newPasswordConfirm = it },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                placeholder = { Text("새로운 비밀번호를 다시 입력하세요") }
            )

            // 에러 메시지 표시
            if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = Color.Red, style = MaterialTheme.typography.bodySmall)
            }

            // 4) 비밀번호 변경 버튼
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    // 비밀번호 변경 로직 (API 호출 등)
                    // 예시: 새 비밀번호 일치 여부 확인
                    if (newPassword != newPasswordConfirm) {
                        errorMessage = "새 비밀번호가 일치하지 않습니다."
                    } else {
                        // 실제로는 뷰모델 통해 비밀번호 변경 API 호출
                        // 성공 시 뒤로가기 등
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("비밀번호 변경")
            }
        }
    }
}
