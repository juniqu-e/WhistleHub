package com.whistlehub.common.view.signup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.whistlehub.R
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.common.view.theme.WhistleHubTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit = {}, // 회원가입 성공 시 호출
    onLoginClick: () -> Unit = {} // 로그인 페이지로 이동
) {
    // 입력 상태 변수 및 에러 변수
    var userId by remember { mutableStateOf("") }
    var userIdError by remember { mutableStateOf<String?>(null) }

    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordConfirm by remember { mutableStateOf("") }
    var passwordConfirmError by remember { mutableStateOf<String?>(null) }

    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }

    var nickname by remember { mutableStateOf("") }
    var nicknameError by remember { mutableStateOf<String?>(null) }

    var gender by remember { mutableStateOf("남성") }

    var birthYear by remember { mutableStateOf("") }
    var birthMonth by remember { mutableStateOf("") }
    var birthDay by remember { mutableStateOf("") }
    var birthError by remember { mutableStateOf<String?>(null) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 포커스 상태 변수
    var isUserIdFocused by remember { mutableStateOf(false) }
    var isPasswordFocused by remember { mutableStateOf(false) }
    var isPasswordConfirmFocused by remember { mutableStateOf(false) }
    var isEmailFocused by remember { mutableStateOf(false) }
    var isNicknameFocused by remember { mutableStateOf(false) }

    val colors = CustomColors()
    val textFieldStyle = Typography.bodySmall.copy(color = colors.Grey50)
    val placeholderStyle = Typography.bodySmall.copy(color = colors.Grey300)
    val buttonTextStyle = Typography.titleSmall.copy(color = colors.Grey950)
    val labelStyle = Typography.labelLarge.copy(color = colors.Grey50)

    // 코루틴 스코프
    val coroutineScope = rememberCoroutineScope()

    // 최상위 레이아웃
    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // 배경 이미지
        Image(
            painter = painterResource(id = R.drawable.login_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // 반투명 오버레이
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
        )
        // 스크롤 가능한 컨테이너
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 800.dp)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 40.dp, vertical = 60.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 로고 영역
                    Text(
                        text = "Whistle Hub Logo",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    // 입력 폼 영역
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // ── 아이디 입력 및 중복 확인 ──
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LabeledInputField(
                                label = "ID",
                                value = userId,
                                onValueChange = { input ->
                                    userId = input
                                    userIdError = if (input.isNotEmpty() && !ValidationUtils.isValidUserId(input)) {
                                        "아이디는 4-20자의 영어 대소문자와 숫자로 구성되어야 합니다."
                                    } else null
                                },
                                placeholder = "아이디를 입력하세요",
                                labelStyle = labelStyle,
                                textStyle = textFieldStyle,
                                placeholderStyle = placeholderStyle,
                                isFocused = isUserIdFocused,
                                onFocusChange = { isUserIdFocused = it },
                                errorMessage = userIdError,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        // TODO: 아이디 중복 확인 API 호출 추가
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.Mint500),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(5.dp),
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .height(24.dp)
                            ) {
                                Text(
                                    text = "중복 확인",
                                    style = textFieldStyle.copy(color = colors.Grey950)
                                )
                            }
                        }

                        // ── 비밀번호 입력 ──
                        LabeledInputField(
                            label = "Password",
                            value = password,
                            onValueChange = { input ->
                                password = input
                                passwordError = if (input.isNotEmpty() && !ValidationUtils.isValidPassword(input)) {
                                    "비밀번호는 8-64자이며, 숫자, 영문 대/소문자, 특수문자를 포함해야 합니다."
                                } else null
                            },
                            placeholder = "비밀번호를 입력하세요",
                            labelStyle = labelStyle,
                            textStyle = textFieldStyle,
                            placeholderStyle = placeholderStyle,
                            isFocused = isPasswordFocused,
                            onFocusChange = { isPasswordFocused = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = PasswordVisualTransformation(),
                            errorMessage = passwordError
                        )

                        // ── 비밀번호 확인 ──
                        LabeledInputField(
                            label = "Password Confirm",
                            value = passwordConfirm,
                            onValueChange = { input ->
                                passwordConfirm = input
                                passwordConfirmError = if (input.isNotEmpty() && input != password) {
                                    "비밀번호가 일치하지 않습니다."
                                } else null
                            },
                            placeholder = "비밀번호를 다시 입력하세요",
                            labelStyle = labelStyle,
                            textStyle = textFieldStyle,
                            placeholderStyle = placeholderStyle,
                            isFocused = isPasswordConfirmFocused,
                            onFocusChange = { isPasswordConfirmFocused = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = PasswordVisualTransformation(),
                            errorMessage = passwordConfirmError
                        )

                        // ── 이메일 입력 및 중복 확인 ──
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LabeledInputField(
                                label = "Email",
                                value = email,
                                onValueChange = { input ->
                                    email = input
                                    emailError = if (input.isNotEmpty() && !ValidationUtils.isValidEmail(input)) {
                                        "유효한 이메일 형식이 아닙니다."
                                    } else null
                                },
                                placeholder = "이메일을 입력하세요",
                                labelStyle = labelStyle,
                                textStyle = textFieldStyle,
                                placeholderStyle = placeholderStyle,
                                isFocused = isEmailFocused,
                                onFocusChange = { isEmailFocused = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                errorMessage = emailError,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        // TODO: 이메일 중복 확인 API 호출 추가
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.Mint500),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(5.dp),
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .height(24.dp)
                            ) {
                                Text(
                                    text = "중복 확인",
                                    style = textFieldStyle.copy(color = colors.Grey950)
                                )
                            }
                        }

                        // ── 닉네임 입력 및 중복 확인 ──
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LabeledInputField(
                                label = "Nickname",
                                value = nickname,
                                onValueChange = { input ->
                                    nickname = input
                                    nicknameError = if (input.isNotEmpty() && !ValidationUtils.isValidNickname(input)) {
                                        "닉네임은 2-20자의 한글과 영어로 구성되어야 합니다."
                                    } else null
                                },
                                placeholder = "닉네임을 입력하세요",
                                labelStyle = labelStyle,
                                textStyle = textFieldStyle,
                                placeholderStyle = placeholderStyle,
                                isFocused = isNicknameFocused,
                                onFocusChange = { isNicknameFocused = it },
                                errorMessage = nicknameError,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        // TODO: 닉네임 중복 확인 API 호출 추가
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.Mint500),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(5.dp),
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .height(24.dp)
                            ) {
                                Text(
                                    text = "중복 확인",
                                    style = textFieldStyle.copy(color = colors.Grey950)
                                )
                            }
                        }

                        // ── 성별 입력 (RadioButton) ──
                        GenderSelection(
                            selectedGender = gender,
                            onGenderSelected = { inputGender -> gender = inputGender },
                            labelStyle = labelStyle,
                            optionTextStyle = textFieldStyle,
                            colors = colors
                        )

                        // ── 생년월일 입력 (Birth) ──
                        BirthDropdownFields(
                            selectedYear = birthYear,
                            onYearSelected = { birthYear = it },
                            selectedMonth = birthMonth,
                            onMonthSelected = { birthMonth = it },
                            selectedDay = birthDay,
                            onDaySelected = { birthDay = it },
                            birthError = if (
                                birthYear.isNotEmpty() &&
                                birthMonth.isNotEmpty() &&
                                birthDay.isNotEmpty() &&
                                !ValidationUtils.isValidBirthDate(birthYear, birthMonth, birthDay)
                            ) {
                                "유효한 생년월일을 입력하세요."
                            } else null,
                            labelStyle = labelStyle,
                            textStyle = textFieldStyle,
                            placeholderStyle = placeholderStyle
                        )

                        // ── 전체 폼 에러 메시지 ──
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        // ── 회원가입 버튼 ──
                        Button(
                            onClick = {
                                if (
                                    userIdError == null &&
                                    passwordError == null &&
                                    passwordConfirmError == null &&
                                    emailError == null &&
                                    nicknameError == null &&
                                    birthError == null &&
                                    userId.isNotEmpty() &&
                                    password.isNotEmpty() &&
                                    passwordConfirm.isNotEmpty() &&
                                    email.isNotEmpty() &&
                                    nickname.isNotEmpty() &&
                                    birthYear.isNotEmpty() &&
                                    birthMonth.isNotEmpty() &&
                                    birthDay.isNotEmpty()
                                ) {
                                    onSignUpSuccess()
                                } else {
                                    errorMessage = "입력값을 확인하세요."
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.Mint500),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                text = "회원가입",
                                style = buttonTextStyle,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        // ── 로그인/비밀번호 찾기 네비게이션 ──
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
                            TextButton(onClick = { /* 추가 옵션 처리 */ }) {
                                Text(text = "비밀번호 찾기", color = colors.Grey50)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=360dp,height=740dp")
@Composable
fun SignUpScreenInteractivePreview() {
    WhistleHubTheme {
        SignUpScreen()
    }
}
