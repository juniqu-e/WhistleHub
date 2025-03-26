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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.whistlehub.R
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.common.view.theme.WhistleHubTheme
import com.whistlehub.common.viewmodel.SignUpViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onNext: (String, String, String, String, Char, String) -> Unit = { _, _, _, _, _, _ ->}, // 정보 입력시 태그선택 화면으로 이동
    onLoginClick: () -> Unit = {}, // 로그인 페이지로 이동
    viewModel: SignUpViewModel = hiltViewModel()
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

    var genderKorean by remember { mutableStateOf("남성") }

    var birthYear by remember { mutableStateOf("") }
    var birthMonth by remember { mutableStateOf("") }
    var birthDay by remember { mutableStateOf("") }
    var birthError by remember { mutableStateOf<String?>(null) }


    var errorMessage by remember { mutableStateOf<String?>(null) }

    var verificationCode by remember { mutableStateOf("") }
    var verificationCodeError by remember { mutableStateOf<String?>(null) }
    var showVerificationInput by remember { mutableStateOf(false) }

    // 포커스 상태 변수
    var isUserIdFocused by remember { mutableStateOf(false) }
    var isPasswordFocused by remember { mutableStateOf(false) }
    var isPasswordConfirmFocused by remember { mutableStateOf(false) }
    var isEmailFocused by remember { mutableStateOf(false) }
    var isNicknameFocused by remember { mutableStateOf(false) }

    val colors = CustomColors()
    val textFieldStyle = Typography.bodyMedium.copy(color = colors.Grey50)
    val placeholderStyle = Typography.bodyMedium.copy(color = colors.Grey300)
    val buttonTextStyle = Typography.titleMedium.copy(color = colors.Grey950)
    val labelStyle = Typography.bodyLarge.copy(color = colors.Grey50)

    // ViewModel의 상태 관찰
    val signUpState by viewModel.signUpState.collectAsState()

    // 아이디 실시간 중복 체크
    LaunchedEffect(userId) {
        if (userId.isEmpty() || !ValidationUtils.isValidUserId(userId)) {
            return@LaunchedEffect
        }
        val currentUserIdError = userIdError
        if (currentUserIdError != null && !currentUserIdError.contains("사용 가능한")) {
            return@LaunchedEffect
        }
        kotlinx.coroutines.delay(500)
        viewModel.checkDuplicateId(userId) { isDuplicate ->
            userIdError = if (isDuplicate)
                "이미 사용 중인 아이디입니다."
            else
                "사용 가능한 아이디입니다."
        }
    }

// 이메일 실시간 중복 체크
    LaunchedEffect(email) {
        if (email.isEmpty() || !ValidationUtils.isValidEmail(email)) {
            return@LaunchedEffect
        }
        val currentEmailError = emailError
        if (currentEmailError != null && !currentEmailError.contains("사용 가능한")) {
            return@LaunchedEffect
        }
        kotlinx.coroutines.delay(500)
        viewModel.checkDuplicateEmail(email) { isDuplicate ->
            emailError = if (isDuplicate)
                "이미 사용 중인 이메일입니다."
            else
                "사용 가능한 이메일입니다."
        }
    }

// 닉네임 실시간 중복 체크
    LaunchedEffect(nickname) {
        if (nickname.isEmpty() || !ValidationUtils.isValidNickname(nickname)) {
            return@LaunchedEffect
        }
        val currentNicknameError = nicknameError
        if (currentNicknameError != null && !currentNicknameError.contains("사용 가능한")) {
            return@LaunchedEffect
        }
        kotlinx.coroutines.delay(500)
        viewModel.checkDuplicateNickname(nickname) { isDuplicate ->
            nicknameError = if (isDuplicate)
                "이미 사용 중인 닉네임입니다."
            else
                "사용 가능한 닉네임입니다."
        }
    }

    // 최상위 레이아웃
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1C))
            .imePadding()
    ) {
//        // 배경 이미지
//        Image(
//            painter = painterResource(id = R.drawable.login_background),
//            contentDescription = null,
//            modifier = Modifier.fillMaxSize(),
//            contentScale = ContentScale.Crop
//        )
//        // 반투명 오버레이
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color.Black.copy(alpha = 0.7f))
//        )
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
                                        "아이디는 4-20자의 대소문자와 숫자로 구성해야 합니다."
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
                        }

                        // ── 비밀번호 입력 ──
                        LabeledInputField(
                            label = "Password",
                            value = password,
                            onValueChange = { input ->
                                password = input
                                passwordError = if (input.isNotEmpty() && !ValidationUtils.isValidPassword(input)) {
                                    "비밀번호는 8-64자이며, 숫자, 대소문자, 특수문자를 포함해야 합니다."
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
                                    // 이메일이 유효할 때만 코드 전송 API 호출
                                    if (email.isEmpty() || !ValidationUtils.isValidEmail(email)) {
                                        emailError = "유효한 이메일 형식이 아닙니다."
                                    } else {
                                        viewModel.sendEmailVerification(email) { message ->
                                            emailError = message
                                            // 성공 메시지일 경우 인증 코드 입력 필드 표시
                                            if (message.contains("전송되었습니다")) {
                                                showVerificationInput = true
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.Mint500),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(5.dp),
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .height(32.dp)
                            ) {
                                Text(
                                    text = "이메일 인증",
                                    style = Typography.labelLarge.copy(
                                        color = colors.Grey950,
                                        fontWeight = FontWeight.Bold
                                    )

                                )
                            }
                        }
                        // ── 인증 코드 입력 및 확인 ──
                        if (showVerificationInput) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LabeledInputField(
                                    label = "코드 입력",
                                    value = verificationCode,
                                    onValueChange = { input ->
                                        verificationCode = input
                                    },
                                    placeholder = "인증 코드를 입력하세요",
                                    labelStyle = labelStyle,
                                    textStyle = textFieldStyle,
                                    placeholderStyle = placeholderStyle,
                                    isFocused = false,
                                    onFocusChange = {},
                                    errorMessage = verificationCodeError,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        // 여기에 이메일 인증 코드 확인 API 호출을 추가합니다.
                                        viewModel.validateEmailCode(
                                            email,
                                            verificationCode
                                        ) { isValid ->
                                            verificationCodeError = if (isValid)
                                                "인증 성공"
                                            else
                                                "인증 코드가 일치하지 않습니다."
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.Mint500),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(5.dp),
                                    modifier = Modifier
                                        .wrapContentWidth()
                                        .height(32.dp)
                                ) {
                                    Text(
                                        text = "코드 확인",
                                        style = Typography.labelLarge.copy(
                                            color = colors.Grey950,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
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
                        }

                        // ── 성별 입력 (RadioButton) ──
                        GenderSelection(
                            selectedGender = genderKorean,
                            onGenderSelected = { inputGender -> genderKorean = inputGender },
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
                                    birthDay.isNotEmpty() &&
                                    verificationCodeError == "인증 성공" // Todo: 인증 성공시 메세지
                                ) {
                                    // gender가 "남성"이면 'M', "여성"이면 'F'로 변환
                                    val gender = if (genderKorean == "남성") 'M' else 'F'
                                    val birth = "$birthYear-$birthMonth-$birthDay"
                                    onNext(userId, password, email, nickname, gender, birth)
                                } else {
                                    errorMessage = "입력값을 확인하세요."
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.Mint500),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                text = "다음",
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
        SignUpScreen(onNext = { _, _, _, _, _, _ -> })
    }
}
