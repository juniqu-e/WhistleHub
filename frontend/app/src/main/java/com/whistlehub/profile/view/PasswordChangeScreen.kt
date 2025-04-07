package com.whistlehub.profile.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.whistlehub.common.util.LogoutManager
import com.whistlehub.common.view.component.CustomAlertDialog
import com.whistlehub.common.view.signup.LabeledInputField
import com.whistlehub.common.view.signup.ValidationUtils
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.profile.viewmodel.PasswordChangeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordChangeScreen(
    logoutManager: LogoutManager,
    navController: NavHostController,
    viewModel: PasswordChangeViewModel = hiltViewModel()
) {
    val colors = CustomColors()
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // 입력 상태 관리
    var currentPassword by remember { mutableStateOf("") }
    var currentPasswordError by remember { mutableStateOf<String?>(null) }
    var isCurrentPasswordFocused by remember { mutableStateOf(false) }

    var newPassword by remember { mutableStateOf("") }
    var newPasswordError by remember { mutableStateOf<String?>(null) }
    var isNewPasswordFocused by remember { mutableStateOf(false) }

    var newPasswordConfirm by remember { mutableStateOf("") }
    var newPasswordConfirmError by remember { mutableStateOf<String?>(null) }
    var isNewPasswordConfirmFocused by remember { mutableStateOf(false) }

    val textFieldStyle = Typography.bodyMedium.copy(color = colors.Grey50)
    val placeholderStyle = Typography.bodyMedium.copy(color = colors.Grey300)
    val labelStyle = Typography.bodyLarge.copy(color = colors.Grey50)
    val buttonTextStyle = Typography.titleMedium.copy(color = colors.Grey950)

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

    // 실패 다이얼로그 표시
    CustomAlertDialog(
        showDialog = uiState.showErrorDialog,
        title = "비밀번호 변경 실패",
        message = uiState.errorMessage,
        onDismiss = { viewModel.dismissErrorDialog() },
        onConfirm = { viewModel.dismissErrorDialog() }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("비밀번호 변경", color = colors.Grey50) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = colors.Grey50
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
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1) 현재 비밀번호
            LabeledInputField(
                label = "현재 Password",
                value = currentPassword,
                onValueChange = {
                    currentPassword = it
                    currentPasswordError = if (it.isEmpty()) "현재 비밀번호를 입력해주세요." else null

                    // 새 비밀번호가 입력된 상태에서 현재 비밀번호를 변경하는 경우, 비교 검사 다시 수행
                    if (newPassword.isNotEmpty() && it == newPassword) {
                        newPasswordError = "기존 비밀번호와 동일합니다."
                    } else if (newPassword.isNotEmpty()) {
                        newPasswordError = if (!ValidationUtils.isValidPassword(newPassword)) {
                            "비밀번호는 8-64자이며, 숫자, 대소문자, 특수문자를 포함해야 합니다."
                        } else {
                            null
                        }
                    }
                },
                placeholder = "현재 비밀번호를 입력하세요",
                labelStyle = labelStyle,
                textStyle = textFieldStyle,
                placeholderStyle = placeholderStyle,
                isFocused = isCurrentPasswordFocused,
                onFocusChange = { isCurrentPasswordFocused = it },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                visualTransformation = PasswordVisualTransformation(),
                errorMessage = currentPasswordError
            )

            // 2) 새로운 비밀번호
            LabeledInputField(
                label = "새로운 Password",
                value = newPassword,
                onValueChange = {
                    newPassword = it

                    // 기존 비밀번호와 동일한지 확인
                    newPasswordError = if (it.isEmpty()) {
                        "새 비밀번호를 입력해주세요."
                    } else if (it == currentPassword) {
                        "기존 비밀번호와 동일합니다."
                    } else if (!ValidationUtils.isValidPassword(it)) {
                        "비밀번호는 8-64자이며, 숫자, 대소문자, 특수문자를 포함해야 합니다."
                    } else {
                        null
                    }

                    // 비밀번호 확인 필드가 비어있지 않다면 일치 여부 확인
                    if (newPasswordConfirm.isNotEmpty()) {
                        newPasswordConfirmError = if (it != newPasswordConfirm) {
                            "비밀번호가 일치하지 않습니다."
                        } else {
                            null
                        }
                    }
                },
                placeholder = "새로운 비밀번호를 입력하세요",
                labelStyle = labelStyle,
                textStyle = textFieldStyle,
                placeholderStyle = placeholderStyle,
                isFocused = isNewPasswordFocused,
                onFocusChange = { isNewPasswordFocused = it },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                visualTransformation = PasswordVisualTransformation(),
                errorMessage = newPasswordError
            )

            // 3) 새로운 비밀번호 확인
            LabeledInputField(
                label = "새로운 Password 확인",
                value = newPasswordConfirm,
                onValueChange = {
                    newPasswordConfirm = it
                    newPasswordConfirmError = if (it.isEmpty()) {
                        "새 비밀번호 확인을 입력해주세요."
                    } else if (it != newPassword) {
                        "비밀번호가 일치하지 않습니다."
                    } else {
                        null
                    }
                },
                placeholder = "새로운 비밀번호를 다시 입력하세요",
                labelStyle = labelStyle,
                textStyle = textFieldStyle,
                placeholderStyle = placeholderStyle,
                isFocused = isNewPasswordConfirmFocused,
                onFocusChange = { isNewPasswordConfirmFocused = it },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                visualTransformation = PasswordVisualTransformation(),
                errorMessage = newPasswordConfirmError
            )

            // 4) 비밀번호 변경 버튼
            Button(
                onClick = {
                    // 로컬 유효성 검사 수행
                    val isCurrentPasswordValid = currentPassword.isNotEmpty()
                    val isNewPasswordValid = newPassword.length >= 8 && newPassword != currentPassword
                    val isNewPasswordConfirmValid = newPassword == newPasswordConfirm

                    // 에러 메시지 업데이트
                    currentPasswordError = if (!isCurrentPasswordValid) "현재 비밀번호를 입력해주세요." else null

                    newPasswordError = if (newPassword.isEmpty()) {
                        "새 비밀번호를 입력해주세요."
                    } else if (newPassword == currentPassword) {
                        "기존 비밀번호와 동일합니다."
                    } else if (!ValidationUtils.isValidPassword(newPassword)) {
                        "비밀번호는 8자 이상이어야 합니다."
                    } else {
                        null
                    }

                    newPasswordConfirmError = if (!isNewPasswordConfirmValid) {
                        if (newPasswordConfirm.isEmpty()) "새 비밀번호 확인을 입력해주세요." else "비밀번호가 일치하지 않습니다."
                    } else null

                    // 모든 유효성 검사를 통과한 경우에만 비밀번호 변경 실행
                    if (isCurrentPasswordValid && isNewPasswordValid && isNewPasswordConfirmValid) {
                        viewModel.changePassword(currentPassword, newPassword)
                    } else {
                        // 유효성 검사 실패 시 다이얼로그로 에러 표시
                        val errorMsg = when {
                            !isCurrentPasswordValid -> "현재 비밀번호를 입력해주세요."
                            newPassword.isEmpty() -> "새 비밀번호를 입력해주세요."
                            newPassword == currentPassword -> "새 비밀번호는 현재 비밀번호와 달라야 합니다."
                            !ValidationUtils.isValidPassword(newPassword) -> "비밀번호는 8-64자이며, 숫자, 대소문자, 특수문자를 포함해야 합니다."
                            !isNewPasswordConfirmValid -> "비밀번호가 일치하지 않습니다."
                            else -> "입력 정보를 확인해주세요."
                        }
                        viewModel.showErrorDialog(errorMsg)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.CommonButtonColor)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = colors.Grey950
                    )
                } else {
                    Text(
                        text = "비밀번호 변경",
                        style = buttonTextStyle,
                        color = colors.CommonTextColor,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
