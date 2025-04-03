package com.whistlehub.profile.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

import com.whistlehub.common.util.LogoutManager
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.profile.view.components.ProfileImageUpload
import com.whistlehub.profile.viewmodel.ProfileChangeViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull

import androidx.compose.runtime.rememberCoroutineScope // 이미 있을 수 있음
import androidx.compose.ui.platform.LocalContext // 추가
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody // 추가
import java.util.UUID // 추가
import android.util.Log // 로깅을 위해 추가
import com.whistlehub.common.view.copmonent.CustomAlertDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileChangeScreen(
    logoutManager: LogoutManager,
    navController: NavHostController,
    viewModel: ProfileChangeViewModel = hiltViewModel()
) {
    val customColors = CustomColors()
    val scope = rememberCoroutineScope()

    // ViewModel의 상태 수집
    val nickname by viewModel.nickname.collectAsState()
    val profileText by viewModel.profileText.collectAsState()
    val profileImageUrl by viewModel.profileImageUrl.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // 로컬 UI 상태
    var nicknameError by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // 다이얼로그 관련 상태
    var showSuccessDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    // 컴포넌트가 처음 로드될 때 프로필 정보 가져오기
    LaunchedEffect(Unit) {
        viewModel.loadProfile(
            viewModel.userRepository.getUser()?.memberId ?: return@LaunchedEffect
        )
    }

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

    val context = LocalContext.current

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
                .padding(16.dp),
        ) {
            ProfileImageUpload(
                profileImageUrl = profileImageUrl,
                onImageSelected = { uri ->
                    selectedImageUri = uri
                    // 이미지가 선택되면 바로 업로드 처리 (메모리 방식)
                    uri?.let { imageUri ->
                        scope.launch {
                            try {
                                // ContentResolver를 사용하여 InputStream 열기
                                context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                                    // InputStream에서 ByteArray로 읽기
                                    val imageBytes = inputStream.readBytes()



                                    // MIME 타입 가져오기 (ContentResolver 사용)
                                    val mimeType = context.contentResolver.getType(imageUri) ?: "image/*" // 기본값 설정
                                    val mediaType = mimeType.toMediaTypeOrNull() // MediaType 객체 미리 생성

                                    // 파일 이름 생성 (Uri에서 가져오거나 UUID 사용)
                                    val filename = viewModel.getFileName(context, imageUri) ?: "${UUID.randomUUID()}.${mimeType.substringAfterLast('/')}"

                                    // ByteArray로부터 RequestBody 생성
                                    val requestBody = imageBytes.toRequestBody(mimeType.toMediaTypeOrNull())

                                    // MultipartBody.Part 생성 ("image" 이름 사용 확인됨)
                                    val imagePart = MultipartBody.Part.createFormData(
                                        "image",
                                        filename,
                                        requestBody
                                    )

                                    // ViewModel 호출
                                    viewModel.updateProfileImage(imagePart)

                                } ?: Log.e("ImageUpload", "Failed to open InputStream for URI: $imageUri")

                            } catch (e: Exception) {
                                Log.e("ImageUpload", "Error processing image URI: $imageUri", e)
                                // 사용자에게 오류 메시지 표시 등 예외 처리
                                // viewModel.showError("이미지 처리 중 오류가 발생했습니다.")
                            }
                        }
                    } ?: Log.w("ImageUpload", "Selected URI is null") // uri가 null인 경우 로그
                },
                onDeleteImage = {
                    viewModel.deleteProfileImage()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2) 닉네임 입력
            Text(text = "Nickname", style = Typography.titleMedium, color = customColors.Grey50)
            OutlinedTextField(
                value = nickname,
                onValueChange = { newValue ->
                    viewModel._nickname.value = newValue
                    // 닉네임 유효성 검사 로직
                    nicknameError = if (newValue.contains(" ")) {
                        "공백 문자는 사용할 수 없습니다."
                    } else ""
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            )

            // 닉네임 오류 메시지
            if (nicknameError.isNotEmpty()) {
                Text(
                    text = nicknameError,
                    color = Color.Red,
                    style = Typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3) 자기소개 입력 (멀티라인)
            Text(text = "Introduce", style = Typography.titleMedium, color = customColors.Grey50)
            OutlinedTextField(
                value = profileText,
                onValueChange = { viewModel._profileText.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp), // 높이를 고정해서 여러 줄 표시
                maxLines = 5,
            )

            // 에러 메시지 표시
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    style = Typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // 4) 하단 버튼 (취소, 수정)
//            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // 취소 버튼
                OutlinedButton(
                    onClick = {
                        navController.popBackStack() // 이전 화면으로 돌아가기
                    }
                ) {
                    Text("취소")
                }

//                Spacer(modifier = Modifier.width(16.dp))

                // 수정 버튼
                Button(
                    onClick = {
                        // 닉네임 유효성 검사 후 업데이트 진행
                        if (nicknameError.isEmpty()) {
                            viewModel.updateProfile(nickname, profileText)
                            // 프로필 업데이트 성공 시 다이얼로그 표시
                            dialogMessage = "프로필이 성공적으로 변경되었습니다."
                            showSuccessDialog = true
//                            navController.popBackStack()
                        }
                    },
                    enabled = !isLoading && nicknameError.isEmpty()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text("수정")
                    }
                }
            }
        }
    }
}
