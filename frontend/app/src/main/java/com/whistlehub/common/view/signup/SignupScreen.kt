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
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.whistlehub.common.viewmodel.EmailVerificationState
import com.whistlehub.common.viewmodel.SignUpViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onNext: (String, String, String, String, Char, String) -> Unit = { _, _, _, _, _, _ -> },
    onLoginClick: () -> Unit = {},
    viewModel: SignUpViewModel = hiltViewModel()
) {
    // 입력 상태 변수들: rememberSaveable 사용하여 저장
    var userId by rememberSaveable { mutableStateOf("") }
    var userIdError by rememberSaveable { mutableStateOf<String?>(null) }
    var userIdValidation by rememberSaveable { mutableStateOf(false) }

    var password by rememberSaveable { mutableStateOf("") }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordConfirm by rememberSaveable { mutableStateOf("") }
    var passwordConfirmError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordValidation by rememberSaveable { mutableStateOf(false) }

    var email by rememberSaveable { mutableStateOf("") }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var emailValidation by rememberSaveable { mutableStateOf(false) }

    var verificationCode by rememberSaveable { mutableStateOf("") }
    var verificationCodeError by rememberSaveable { mutableStateOf<String?>(null) }
    var showVerificationInput by rememberSaveable { mutableStateOf(false) }

    var nickname by rememberSaveable { mutableStateOf("") }
    var nicknameError by rememberSaveable { mutableStateOf<String?>(null) }
    var nicknameValidation by rememberSaveable { mutableStateOf(false) }

    var genderKorean by rememberSaveable { mutableStateOf("남성") }

    var birthYear by rememberSaveable { mutableStateOf("") }
    var birthMonth by rememberSaveable { mutableStateOf("") }
    var birthDay by rememberSaveable { mutableStateOf("") }
    var birthError by rememberSaveable { mutableStateOf<String?>(null) }
    var birthValidation by rememberSaveable { mutableStateOf(false) }

    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    // 포커스 상태 변수
    var isUserIdFocused by remember { mutableStateOf(false) }
    var isPasswordFocused by remember { mutableStateOf(false) }
    var isPasswordConfirmFocused by remember { mutableStateOf(false) }
    var isEmailFocused by remember { mutableStateOf(false) }
    var isNicknameFocused by remember { mutableStateOf(false) }
    var isVerificationCodeFocused by remember { mutableStateOf(false) }


    val colors = CustomColors()
    val textFieldStyle = Typography.bodyMedium.copy(color = colors.Grey50)
    val placeholderStyle = Typography.bodyMedium.copy(color = colors.Grey300)
    val buttonTextStyle = Typography.titleMedium.copy(color = colors.Grey950)
    val labelStyle = Typography.bodyLarge.copy(color = colors.Grey50)

    // ViewModel의 상태 관찰
    val emailVerificationState by viewModel.emailVerificationState.collectAsState()

    // 아이디 실시간 중복 체크
    LaunchedEffect(userId) {
        if (userId.isEmpty() || !ValidationUtils.isValidUserId(userId)) return@LaunchedEffect
        val currentUserIdError = userIdError
        if (currentUserIdError != null && !currentUserIdError.contains("사용 가능한")) return@LaunchedEffect
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
        if (email.isEmpty() || !ValidationUtils.isValidEmail(email)) return@LaunchedEffect
        val currentEmailError = emailError
        if (currentEmailError != null && !currentEmailError.contains("사용 가능한")) return@LaunchedEffect
        kotlinx.coroutines.delay(500)
        viewModel.checkDuplicateEmail(email) { isDuplicate ->
            emailError = if (isDuplicate)
                "이미 사용 중인 이메일입니다."
            else
                "사용 가능한 이메일입니다."
        }
    }

    // 이메일 인증 상태 변화에 따른 메시지 업데이트
    LaunchedEffect(emailVerificationState) {
        when (emailVerificationState) {
            is EmailVerificationState.Sent -> {
                emailError = (emailVerificationState as EmailVerificationState.Sent).message
            }
            is EmailVerificationState.Verified -> {
                verificationCodeError = (emailVerificationState as EmailVerificationState.Verified).message
            }
            is EmailVerificationState.Error -> {
                if (showVerificationInput)
                    verificationCodeError = (emailVerificationState as EmailVerificationState.Error).message
                else
                    emailError = (emailVerificationState as EmailVerificationState.Error).message
            }
            else -> { }
        }
    }

    // 닉네임 실시간 중복 체크
    LaunchedEffect(nickname) {
        if (nickname.isEmpty() || !ValidationUtils.isValidNickname(nickname)) return@LaunchedEffect
        val currentNicknameError = nicknameError
        if (currentNicknameError != null && !currentNicknameError.contains("사용 가능한")) return@LaunchedEffect
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
                        // ── 아이디 입력 및 중복 확인 (최대 20자) ──
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LabeledInputField(
                                label = "ID",
                                value = userId,
                                onValueChange = { input ->
                                    // 20자 제한
                                    userId = input.take(20)
                                    userIdError = if (userId.isNotEmpty() && !ValidationUtils.isValidUserId(userId))
                                        "아이디는 4-20자의 대소문자와 숫자로 구성해야 합니다." else null
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
                        // ── 비밀번호 입력 (최대 64자) ──
                        LabeledInputField(
                            label = "Password",
                            value = password,
                            onValueChange = { input ->
                                password = input.take(64)
                                passwordError = if (password.isNotEmpty() && !ValidationUtils.isValidPassword(password))
                                    "비밀번호는 8-64자이며, 숫자, 대소문자, 특수문자를 포함해야 합니다." else null
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
                        // ── 비밀번호 확인 (최대 64자) ──
                        LabeledInputField(
                            label = "Password Confirm",
                            value = passwordConfirm,
                            onValueChange = { input ->
                                passwordConfirm = input.take(64)
                                passwordConfirmError = if (passwordConfirm.isNotEmpty() && passwordConfirm != password)
                                    "비밀번호가 일치하지 않습니다." else null
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
                        // ── 이메일 입력 및 중복 확인 (최대 100자) ──
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LabeledInputField(
                                label = "Email",
                                value = email,
                                onValueChange = { input ->
                                    email = input.take(100)
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
                                    if (email.isEmpty() || !ValidationUtils.isValidEmail(email)) {
                                        emailError = "유효한 이메일 형식이 아닙니다."
                                    } else {
                                        viewModel.sendEmailVerification(email)
                                        showVerificationInput = true
                                    }
                                },
                                enabled = emailVerificationState !is EmailVerificationState.Sending,
                                colors = ButtonDefaults.buttonColors(containerColor = colors.CommonButtonColor),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(5.dp),
                                modifier = Modifier.wrapContentWidth().height(32.dp)
                            ) {
                                if (emailVerificationState is EmailVerificationState.Sending)
                                    Text(
                                        text = "로딩중...",
                                        style = Typography.labelLarge.copy(
                                            color = colors.CommonTextColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                else
                                    Text(
                                        text = "이메일 인증",
                                        style = Typography.labelLarge.copy(
                                            color = colors.CommonTextColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                            }
                        }
                        // ── 인증 코드 입력 및 확인 ──
                        if (showVerificationInput && emailVerificationState !is EmailVerificationState.Sending) {
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
                                    isFocused = isVerificationCodeFocused,
                                    onFocusChange = { isVerificationCodeFocused = it },
                                    errorMessage = verificationCodeError,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.validateEmailCode(email, verificationCode)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.CommonButtonColor),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(5.dp),
                                    modifier = Modifier.wrapContentWidth().height(32.dp)
                                ) {
                                    Text(
                                        text = "코드 확인",
                                        style = Typography.labelLarge.copy(
                                            color = colors.CommonTextColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                        // ── 닉네임 입력 및 중복 확인 (최대 20자) ──
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LabeledInputField(
                                label = "Nickname",
                                value = nickname,
                                onValueChange = { input ->
                                    nickname = input.take(20)
                                    nicknameError = if (nickname.isNotEmpty() && !ValidationUtils.isValidNickname(nickname))
                                        "닉네임은 2-20자의 한글과 영어로 구성되어야 합니다." else null
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
                        // ── 다음 버튼 ──
                        Button(
                            onClick = {
                                if (
                                    userId.isNotEmpty() &&
                                    password.isNotEmpty() &&
                                    passwordConfirm.isNotEmpty() &&
                                    email.isNotEmpty() &&
                                    nickname.isNotEmpty() &&
                                    birthYear.isNotEmpty() &&
                                    birthMonth.isNotEmpty() &&
                                    birthDay.isNotEmpty() &&
                                    viewModel.isEmailVerificationRequested
                                ) {
                                    val gender = if (genderKorean == "남성") 'M' else 'F'
                                    val birth = "$birthYear-$birthMonth-$birthDay"
                                    onNext(userId, password, email, nickname, gender, birth)
                                } else {
                                    errorMessage = "입력값을 확인하세요."
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.CommonButtonColor),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                text = "다음",
                                style = buttonTextStyle,
                                color = colors.CommonTextColor,
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
