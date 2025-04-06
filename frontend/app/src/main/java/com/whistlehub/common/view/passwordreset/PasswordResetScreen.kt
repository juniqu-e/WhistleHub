package com.whistlehub.common.view.passwordreset

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.whistlehub.R
import com.whistlehub.common.view.signup.LabeledInputField
import com.whistlehub.common.view.signup.ValidationUtils
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.common.viewmodel.PasswordResetState
import com.whistlehub.common.viewmodel.PasswordResetViewModel

@Composable
fun PasswordResetScreen(
    onLoginClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
    navController: NavHostController,
    viewModel: PasswordResetViewModel = hiltViewModel()
) {
    // 키보드 컨트롤러
    val keyboardController = LocalSoftwareKeyboardController.current

    // 상태 관리
    var userId by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isUserIdFocused by remember { mutableStateOf(false) }
    var isEmailFocused by remember { mutableStateOf(false) }
    var userIdError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // ViewModel의 상태를 구독
    val resetState by viewModel.resetState.collectAsState()

    // 상태에 따른 이벤트 처리
    LaunchedEffect(resetState) {
        when (resetState) {
            is PasswordResetState.Success -> {
                // 성공 시 처리
                errorMessage = null
                // 여기서 비밀번호 재설정 이메일이 전송되었다는 메시지를 표시할 수 있습니다.
                // 또는 새 비밀번호 설정 화면으로 이동할 수 있습니다.
            }
            is PasswordResetState.Error -> {
                errorMessage = (resetState as PasswordResetState.Error).message
            }
            else -> { /* Idle 또는 Loading 상태는 별도 처리 */ }
        }
    }

    val colors = CustomColors()
    val textFieldStyle = Typography.bodyMedium.copy(color = colors.CommonTextColor)
    val placeholderStyle = Typography.bodyMedium.copy(color = colors.CommonPlaceholderColor)
    val buttonTextStyle = Typography.titleMedium.copy(color = colors.CommonTextColor)
    val labelStyle = Typography.bodyLarge.copy(color = colors.Grey50)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // 배경
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.CommonBackgroundColor)
        )
        // 중앙 정렬을 위한 Box
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // 콘텐츠의 최대 너비를 제한하는 Box
            Box(
                modifier = Modifier
                    .widthIn(max = 800.dp)
                    .fillMaxHeight()
            ) {
                @Suppress("UnusedBoxWithConstraintsScope") // Lint 오류방지 코드
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val verticalPadding = when {
                        maxHeight < 500.dp -> 30.dp
                        maxHeight < 700.dp -> 60.dp
                        maxHeight < 800.dp -> 120.dp
                        else -> 150.dp
                    }
                    // 전체 화면을 채우는 Column
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 40.dp, vertical = verticalPadding),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 최상단: 로고
                        Image(
                            painter = painterResource(id = R.drawable.whistlehub_mainlogo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .padding(bottom = 20.dp),
                        )

                        // 비밀번호 초기화 안내 텍스트
                        Text(
                            text = "비밀번호 초기화",
                            style = Typography.titleLarge,
                            color = colors.Grey50,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Text(
                            text = "아이디와 이메일을 입력하시면 비밀번호 재설정 링크를 보내드립니다.",
                            style = Typography.bodyMedium,
                            color = colors.Grey300,
                            modifier = Modifier.padding(bottom = 32.dp)
                        )

                        // 입력 폼 영역
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 아이디 입력 필드
                            LabeledInputField(
                                label = "ID",
                                value = userId,
                                onValueChange = { input ->
                                    userId = input
                                    userIdError = if (input.isNotEmpty() && !ValidationUtils.isValidUserId(input))
                                        "아이디는 4-20자의 대소문자와 숫자로 구성해야 합니다."
                                    else null
                                },
                                placeholder = "아이디를 입력하세요",
                                labelStyle = labelStyle,
                                textStyle = textFieldStyle,
                                placeholderStyle = placeholderStyle,
                                isFocused = isUserIdFocused,
                                onFocusChange = { isUserIdFocused = it },
                                errorMessage = userIdError
                            )

                            // 이메일 입력 필드
                            LabeledInputField(
                                label = "Email",
                                value = email,
                                onValueChange = { input ->
                                    email = input
                                    emailError = if (input.isNotEmpty() && !ValidationUtils.isValidEmail(input))
                                        "올바른 이메일 형식이 아닙니다."
                                    else null
                                },
                                placeholder = "이메일을 입력하세요",
                                labelStyle = labelStyle,
                                textStyle = textFieldStyle,
                                placeholderStyle = placeholderStyle,
                                isFocused = isEmailFocused,
                                onFocusChange = { isEmailFocused = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                errorMessage = emailError
                            )

                            // 에러 메시지 또는 성공 메시지 표시
                            if (errorMessage != null) {
                                Text(
                                    text = errorMessage!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(8.dp)
                                )
                            } else if (resetState is PasswordResetState.Success) {
                                Text(
                                    text = (resetState as PasswordResetState.Success).message ?: "임시 비밀번호가 이메일로 발송되었습니다.",
                                    color = colors.Mint500,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }

                            // 비밀번호 초기화 요청 버튼
                            Button(
                                onClick = {
                                    keyboardController?.hide()
                                    viewModel.resetPassword(userId, email)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.CommonButtonColor
                                ),
                                shape = RoundedCornerShape(8.dp),
                                enabled = resetState != PasswordResetState.Loading && // 로딩 중이 아닐 때
                                        userId.isNotEmpty() && email.isNotEmpty() && // 빈 값이 아닐 때
                                        userIdError == null && emailError == null // 에러가 없을 때
                            ) {
                                if (resetState == PasswordResetState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = colors.CommonTextColor,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "비밀번호 초기화",
                                        style = buttonTextStyle,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                            // 로그인/회원가입 네비게이션
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = onLoginClick) {
                                    Text(text = "로그인", color = colors.Grey50)
                                }
                                Spacer(modifier = Modifier.width(20.dp))
                                Text(text = "|", color = colors.Grey50)
                                Spacer(modifier = Modifier.width(20.dp))
                                TextButton(onClick = onSignUpClick) {
                                    Text(text = "회원가입", color = colors.Grey50)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}