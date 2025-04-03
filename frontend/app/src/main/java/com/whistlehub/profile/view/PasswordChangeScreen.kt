package com.whistlehub.profile.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.whistlehub.common.util.LogoutManager
import com.whistlehub.common.view.component.CustomAlertDialog
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.profile.viewmodel.PasswordChangeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordChangeScreen(
    logoutManager: LogoutManager,
    navController: NavHostController,
    // 필요하다면 뷰모델을 주입해서 API 연동, 상태 관리
     viewModel: PasswordChangeViewModel = hiltViewModel()
) {
    val customColors = CustomColors()
    val uiState by viewModel.uiState.collectAsState()

    // UI 상태(예시). 실제로는 뷰모델 상태를 collectAsState()로 가져올 수 있음
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var newPasswordConfirm by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") } // 예: "새 비밀번호가 일치하지 않습니다." 등

    // 성공 다이얼로그 표시
    CustomAlertDialog(
        showDialog = uiState.showSuccessDialog,
        title = "비밀번호 변경 완료",
        message = uiState.dialogMessage,
        onDismiss = { viewModel.dismissDialog() },
        onConfirm = {
            viewModel.dismissDialog()
            navController.popBackStack() // 다이얼로그 확인 후 이전 화면으로 이동
        }
    )

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
            if (uiState.errorMessage.isNotEmpty()) {
                Text(text = uiState.errorMessage, color = Color.Red, style = MaterialTheme.typography.bodySmall)
            }

            // 4) 비밀번호 변경 버튼
//            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    if (newPassword == newPasswordConfirm) {
                        viewModel.changePassword(currentPassword, newPassword)
                    } else {
                        // 로컬 유효성 검사 에러
                        // 뷰모델 상태를 업데이트하는 대신 로컬 상태 사용
//                        errorMessage = "새 비밀번호가 일치하지 않습니다."
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("비밀번호 변경")
                }
            }
        }
    }
}
