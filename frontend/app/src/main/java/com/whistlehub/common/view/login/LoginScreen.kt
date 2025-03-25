package com.whistlehub.common.view.login

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.WhistleHubTheme
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import com.whistlehub.R
import com.whistlehub.common.viewmodel.LoginViewModel
import com.whistlehub.common.viewmodel.LoginState


@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {
    // 아이디/비밀번호 상태 관리
    var userId by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }
    var isUserIdFocused by remember { mutableStateOf(false) }
    var isPasswordFocused by remember { mutableStateOf(false) }

    // 로그인 결과에 따른 UI 처리 (예: 에러 메시지 출력)
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // ViewModel의 로그인 상태를 구독합니다.
    val loginState by viewModel.loginState.collectAsState()

    // 로그인 상태에 따른 이벤트 처리
    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Success -> {
                onLoginSuccess()
                viewModel.resetState()
            }
            is LoginState.Error -> {
                errorMessage = (loginState as LoginState.Error).message
            }
            else -> { /* Idle 또는 Loading 상태는 별도 처리 */ }
        }
    }

    // 커스텀 색상 객체 생성
    val colors = CustomColors()

    // 텍스트 스타일 정의
    val textFieldStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
    val placeholderStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.7f))

    Box(modifier = Modifier.fillMaxSize()) {
        // 배경 이미지
        Image(
            painter = painterResource(id = R.drawable.login_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // 검은색 가림막 (투명도 70%)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
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
                        Text(
                            text = "Whistle Hub Logo",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        // 최하단: 폼 영역
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 아이디 입력 필드
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .drawBehind {
                                        val strokeWidth = 1.dp.toPx()
                                        val y = size.height
                                        drawLine(
                                            color = if (isUserIdFocused) Color.White else Color.White.copy(alpha = 0.7f),
                                            start = Offset(0f, y),
                                            end = Offset(size.width, y),
                                            strokeWidth = strokeWidth
                                        )
                                    }
                            ) {
                                BasicTextField(
                                    value = userId,
                                    onValueChange = { userId = it },
                                    textStyle = textFieldStyle,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.CenterStart)
                                        .onFocusChanged { isUserIdFocused = it.isFocused },
                                    singleLine = true,
                                    interactionSource = remember { MutableInteractionSource() },
                                    decorationBox = { innerTextField ->
                                        Box {
                                            if (userId.isEmpty()) {
                                                Text(
                                                    text = "아이디를 입력하세요",
                                                    style = placeholderStyle
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                            }
                            // 비밀번호 입력 필드
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .drawBehind {
                                        val strokeWidth = 1.dp.toPx()
                                        val y = size.height
                                        drawLine(
                                            color = if (isPasswordFocused) Color.White else Color.White.copy(alpha = 0.7f),
                                            start = Offset(0f, y),
                                            end = Offset(size.width, y),
                                            strokeWidth = strokeWidth
                                        )
                                    }
                            ) {
                                BasicTextField(
                                    value = userPassword,
                                    onValueChange = { userPassword = it },
                                    textStyle = textFieldStyle,
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.CenterStart)
                                        .onFocusChanged { isPasswordFocused = it.isFocused },
                                    singleLine = true,
                                    interactionSource = remember { MutableInteractionSource() },
                                    decorationBox = { innerTextField ->
                                        Box {
                                            if (userPassword.isEmpty()) {
                                                Text(
                                                    text = "비밀번호를 입력하세요",
                                                    style = placeholderStyle
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )
                            }
                            // 에러 메시지 표시
                            if (errorMessage != null) {
                                Text(
                                    text = errorMessage!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            // 로그인 버튼
                            Button(
                                onClick = { viewModel.login(userId, userPassword) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.Mint500),
                                shape = RoundedCornerShape(8.dp),
                                enabled = loginState != LoginState.Loading // 로딩 시 비활성화
                            ) {
                                if (loginState == LoginState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "로그인",
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                            // 회원가입/비밀번호 찾기 행
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = {
                                    Log.d("LoginScreen", "회원가입 버튼 클릭")
                                    onSignUpClick()
                                }
                                ) {
                                    Text(text = "회원가입", color = Color.White)
                                }
                                Spacer(modifier = Modifier.width(20.dp))
                                Text(text = "|", color = Color.White)
                                Spacer(modifier = Modifier.width(20.dp))
                                TextButton(onClick = onForgotPasswordClick) {
                                    Text(text = "비밀번호 찾기", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

