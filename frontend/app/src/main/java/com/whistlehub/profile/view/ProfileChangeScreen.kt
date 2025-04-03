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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.whistlehub.common.util.LogoutManager
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.profile.view.components.ProfileImageUpload
import com.whistlehub.profile.viewmodel.ProfileChangeViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

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

    // 컴포넌트가 처음 로드될 때 프로필 정보 가져오기
    LaunchedEffect(Unit) {
        viewModel.loadProfile(
            viewModel.userRepository.getUser()?.memberId ?: return@LaunchedEffect
        )
    }

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
            // 1) 프로필 이미지 및 버튼
            ProfileImageUpload(
                profileImageUrl = profileImageUrl,
                onImageSelected = { uri ->
                    selectedImageUri = uri
                    // 이미지가 선택되면 바로 업로드 처리
                    uri?.let { imageUri ->
                        scope.launch {
                            val file = File(imageUri.path ?: return@launch)
                            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                            val imagePart = MultipartBody.Part.createFormData(
                                "image",
                                file.name,
                                requestFile
                            )
                            viewModel.updateProfileImage(imagePart)
                        }
                    }
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
            Spacer(modifier = Modifier.weight(1f))
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
                            navController.popBackStack()
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
