package com.whistlehub.common.view.signup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import com.whistlehub.common.view.theme.Typography
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.whistlehub.R
import com.whistlehub.common.view.copmonent.BirthDropdownFields
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.WhistleHubTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    // 예시로 상태 변수 선언
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

    // 생년월일
    var birthYear by remember { mutableStateOf("") }
    var birthMonth by remember { mutableStateOf("") }
    var birthDay by remember { mutableStateOf("") }
    var birthError by remember { mutableStateOf<String?>(null) }

    // 포커스 상태
    var isUserIdFocused by remember { mutableStateOf(false) }
    var isPasswordFocused by remember { mutableStateOf(false) }
    var isPasswordConfirmFocused by remember { mutableStateOf(false) }
    var isEmailFocused by remember { mutableStateOf(false) }
    var isNicknameFocused by remember { mutableStateOf(false) }
    // Birth 포커스 상태 (년/월/일 각각 관리)
    var isBirthYearFocused by remember { mutableStateOf(false) }
    var isBirthMonthFocused by remember { mutableStateOf(false) }
    var isBirthDayFocused by remember { mutableStateOf(false) }

    // 전체 폼 에러 메시지
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 커스텀 색상 및 텍스트 스타일
    val colors = CustomColors()
    val textFieldStyle = Typography.bodySmall.copy(color = CustomColors().Grey50)
    val placeholderStyle = Typography.bodySmall.copy(color = CustomColors().Grey300)
    val buttonTextStyle = Typography.titleSmall.copy(color = CustomColors().Grey950)
    val labelStyle = Typography.labelLarge.copy(color = colors.Grey50)

    // (테스트용으로 에러 표시)
    userIdError = "이미 존재하는 아이디입니다."

    Box(modifier = Modifier.fillMaxSize()) {
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
        // 스크롤 컨테이너
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
                @Suppress("UnusedBoxWithConstraintsScope")
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val verticalPadding = 60.dp
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 40.dp, vertical = verticalPadding),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 로고
                        Text(
                            text = "Whistle Hub Logo",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        // 폼 영역
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // 아이디
                            LabeledCustomInputField(
                                label = "ID",
                                value = userId,
                                onValueChange = {
                                    userId = it
                                    userIdError = if (userId == "admin") {
                                        "이미 존재하는 아이디입니다."
                                    } else null
                                },
                                placeholder = "아이디를 입력하세요",
                                labelStyle = labelStyle,
                                textStyle = textFieldStyle,
                                placeholderStyle = placeholderStyle,
                                isFocused = isUserIdFocused,
                                onFocusChange = { isUserIdFocused = it },
                                errorMessage = userIdError
                            )

                            // 비밀번호
                            LabeledCustomInputField(
                                label = "Password",
                                value = password,
                                onValueChange = {
                                    password = it
                                    passwordError = if (password.length < 8) {
                                        "비밀번호는 8자 이상이어야 합니다."
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

                            // 비밀번호 확인
                            LabeledCustomInputField(
                                label = "Password Confirm",
                                value = passwordConfirm,
                                onValueChange = {
                                    passwordConfirm = it
                                    passwordConfirmError = if (passwordConfirm != password) {
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

                            // 이메일
                            LabeledCustomInputField(
                                label = "Email",
                                value = email,
                                onValueChange = {
                                    email = it
                                    emailError = if (!email.contains("@")) {
                                        "이메일 형식이 아닙니다."
                                    } else null
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

                            // 닉네임
                            LabeledCustomInputField(
                                label = "Nickname",
                                value = nickname,
                                onValueChange = {
                                    nickname = it
                                    nicknameError = if (nickname == "admin") {
                                        "이미 존재하는 닉네임입니다."
                                    } else null
                                },
                                placeholder = "닉네임을 입력하세요",
                                labelStyle = labelStyle,
                                textStyle = textFieldStyle,
                                placeholderStyle = placeholderStyle,
                                isFocused = isNicknameFocused,
                                onFocusChange = { isNicknameFocused = it },
                                errorMessage = nicknameError
                            )

                            // 성별 입력 (RadioButton 그룹)
                            GenderSelection(
                                selectedGender = "남성", // 기본값은 필요에 따라 관리
                                onGenderSelected = { /* 성별 상태 업데이트 */ },
                                labelStyle = labelStyle,
                                optionTextStyle = textFieldStyle,
                                colors = colors
                            )

                            // 생년월일 (Birth)
                            BirthDropdownFields(
                                selectedYear = birthYear,
                                onYearSelected = { birthYear = it },
                                selectedMonth = birthMonth,
                                onMonthSelected = { birthMonth = it },
                                selectedDay = birthDay,
                                onDaySelected = { birthDay = it },
                                birthError = birthError,
                                labelStyle = labelStyle,
                                textStyle = textFieldStyle,
                                placeholderStyle = placeholderStyle
                            )

                            // 전체 폼 레벨 에러 메시지
                            if (errorMessage != null) {
                                Text(
                                    text = errorMessage!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }

                            // 회원가입 버튼
                            Button(
                                onClick = {
                                    if (
                                        userIdError == null &&
                                        passwordError == null &&
                                        passwordConfirmError == null &&
                                        emailError == null &&
                                        nicknameError == null &&
                                        birthError == null
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

                            // 로그인 페이지로 이동
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = onLoginClick) {
                                    Text(text = "로그인", color = Color.White)
                                }
                                Spacer(modifier = Modifier.width(20.dp))
                                Text(text = "|", color = Color.White)
                                Spacer(modifier = Modifier.width(20.dp))
                                TextButton(onClick = { /* 비밀번호 찾기 등 추가 옵션 */ }) {
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

/**
 * GenderSelection: 성별 입력을 위한 RadioButton 그룹 (선택된 항목은 민트색)
 */
@Composable
fun GenderSelection(
    selectedGender: String,
    onGenderSelected: (String) -> Unit,
    labelStyle: TextStyle,
    optionTextStyle: TextStyle,
    colors: CustomColors,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Gender",
            style = labelStyle
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedGender == "남성",
                onClick = { onGenderSelected("남성") },
                colors = RadioButtonDefaults.colors(
                    selectedColor = colors.Mint500,
                    unselectedColor = Color.White
                )
            )
            Text(
                text = "남성",
                style = optionTextStyle,
                modifier = Modifier.padding(end = 16.dp)
            )
            RadioButton(
                selected = selectedGender == "여성",
                onClick = { onGenderSelected("여성") },
                colors = RadioButtonDefaults.colors(
                    selectedColor = colors.Mint500,
                    unselectedColor = Color.White
                )
            )
            Text(
                text = "여성",
                style = optionTextStyle
            )
        }
    }
}

/**
 * 기존 LabeledCustomInputField 예시 (다른 필드용)
 */
@Composable
fun LabeledCustomInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    labelStyle: TextStyle,
    textStyle: TextStyle,
    placeholderStyle: TextStyle,
    isFocused: Boolean,
    onFocusChange: (Boolean) -> Unit,
    errorMessage: String? = null,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Box: 라벨 + 입력 필드 + drawBehind 라인
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .drawBehind {
                    val strokeWidth = 1.dp.toPx()
                    val y = size.height - strokeWidth
                    drawLine(
                        color = if (isFocused) Color.White else Color.White.copy(alpha = 0.7f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokeWidth
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
            ) {
                // 라벨
                Text(
                    text = label,
                    style = labelStyle
                )
                Spacer(modifier = Modifier.height(12.dp))
                // 입력 필드
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = textStyle,
                    keyboardOptions = keyboardOptions,
                    visualTransformation = visualTransformation,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { onFocusChange(it.isFocused) },
                    interactionSource = remember { MutableInteractionSource() },
                    decorationBox = { innerTextField ->
                        Box {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = placeholderStyle
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
        // 에러 메시지 영역 (항상 고정 높이)
        Box(modifier = Modifier.height(30.dp)) {
            if (!errorMessage.isNullOrEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
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
