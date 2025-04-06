package com.whistlehub.profile.view

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.whistlehub.common.util.LogoutManager
import com.whistlehub.common.util.uriToMultipartBodyPart
import com.whistlehub.common.view.component.CustomAlertDialog
import com.whistlehub.common.view.signup.LabeledInputField
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.profile.viewmodel.ProfileChangeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileChangeScreen(
    logoutManager: LogoutManager,
    navController: NavHostController,
    viewModel: ProfileChangeViewModel = hiltViewModel()
) {
    val customColors = CustomColors()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // ViewModel의 상태 수집
    val nickname by viewModel.nickname.collectAsState()
    val profileText by viewModel.profileText.collectAsState()
    val profileImageUrl by viewModel.profileImageUrl.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // 로컬 UI 상태
    var nicknameError by remember { mutableStateOf<String?>(null) }
    var isNicknameFocused by remember { mutableStateOf(false) }

    var profileTextError by remember { mutableStateOf<String?>(null) }
    var isProfileTextFocused by remember { mutableStateOf(false) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showDeleteImageConfirm by remember { mutableStateOf(false) }

    // 다이얼로그 관련 상태
    var showSuccessDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    // 갤러리 접근 런처
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            scope.launch {
                try {
                    val imagePart = uriToMultipartBodyPart(context, it)
                    viewModel.updateProfileImage(imagePart)
                } catch (e: Exception) {
                    Log.e("ProfileChangeScreen", "Error processing image: ${e.message}")
                }
            }
        }
    }

    // 컴포넌트가 처음 로드될 때 프로필 정보 가져오기
    LaunchedEffect(Unit) {
        viewModel.loadProfile(
            viewModel.userRepository.getUser()?.memberId ?: return@LaunchedEffect
        )
    }

    // 성공 다이얼로그
    CustomAlertDialog(
        showDialog = showSuccessDialog,
        title = "프로필 변경 완료",
        message = dialogMessage,
        onDismiss = { showSuccessDialog = false },
        onConfirm = {
            showSuccessDialog = false
            navController.popBackStack() // 다이얼로그 확인 후 이전 화면으로 이동
        }
    )

    // 이미지 삭제 확인 다이얼로그
    CustomAlertDialog(
        showDialog = showDeleteImageConfirm,
        title = "프로필 이미지 삭제",
        message = "프로필 이미지를 삭제하시겠습니까?",
        onDismiss = { showDeleteImageConfirm = false },
        onConfirm = {
            viewModel.deleteProfileImage()
            showDeleteImageConfirm = false
        }
    )

    val textFieldStyle = Typography.bodyMedium.copy(color = customColors.Grey50)
    val placeholderStyle = Typography.bodyMedium.copy(color = customColors.Grey300)
    val labelStyle = Typography.bodyLarge.copy(color = customColors.Grey50)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("회원정보 수정", color = customColors.Grey50) },
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
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 프로필 이미지 업로드 UI (개선된 버전)
            Box(
                modifier = Modifier,
                contentAlignment = Alignment.Center
            ) {
                // 프로필 이미지
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(customColors.Grey800)
                        .border(2.dp, customColors.Grey500, CircleShape)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "프로필 이미지",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (profileImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = profileImageUrl,
                            contentDescription = "프로필 이미지",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "기본 프로필",
                            tint = customColors.Grey300,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }

                // 카메라 아이콘 - 프로필 이미지 밖에 배치
                Box(
                    modifier = Modifier
                        .padding(start = 90.dp, top = 90.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(customColors.Grey800)
                        .border(1.dp, customColors.Grey500, CircleShape)
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "사진 선택",
                        tint = customColors.Grey50,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // 이미지 관련 액션 버튼
            if (profileImageUrl.isNotEmpty() || selectedImageUri != null) {
                Button(
                    onClick = { showDeleteImageConfirm = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = customColors.Error700
                    ),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text("이미지 삭제")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 닉네임 입력 필드 (LabeledInputField 사용)
            LabeledInputField(
                label = "Nickname",
                value = nickname,
                onValueChange = {
                    viewModel._nickname.value = it
                    nicknameError = if (it.contains(" ")) "공백 문자는 사용할 수 없습니다." else null
                },
                placeholder = "닉네임을 입력하세요",
                labelStyle = labelStyle,
                textStyle = textFieldStyle,
                placeholderStyle = placeholderStyle,
                isFocused = isNicknameFocused,
                onFocusChange = { isNicknameFocused = it },
                errorMessage = nicknameError,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 자기소개 입력 필드 (LabeledInputField 사용)
            LabeledInputField(
                label = "Description",
                value = profileText,
                onValueChange = {
                    if (it.length <= 48) {
                        viewModel._profileText.value = it
                    }
                },
                placeholder = "자기소개를 입력하세요",
                labelStyle = labelStyle,
                textStyle = textFieldStyle,
                placeholderStyle = placeholderStyle,
                isFocused = isProfileTextFocused,
                onFocusChange = { isProfileTextFocused = it },
                errorMessage = profileTextError,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Default // ✨ 줄바꿈 허용
                ),
                maxLines = 2
            )

            // 에러 메시지 표시
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = customColors.Error700,
                    style = Typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // 저장 버튼
            Button(
                onClick = {
                    if (nicknameError == null) {
                        viewModel.updateProfile(nickname, profileText)
                        dialogMessage = "프로필이 성공적으로 변경되었습니다."
                        showSuccessDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && nicknameError == null,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = customColors.CommonButtonColor
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = customColors.Grey950,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "변경사항 저장",
                        style = Typography.titleMedium,
                        color = customColors.CommonTextColor,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}