package com.whistlehub.profile.view

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
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.whistlehub.common.util.LogoutManager
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileChangeScreen(
    logoutManager: LogoutManager,
    navController: NavHostController,
    // 필요하다면 뷰모델을 주입해서 API 연동, 상태 관리
    // viewModel: ProfileEditViewModel = hiltViewModel()
) {
    val customColors = CustomColors()

    // UI 상태(예시). 실제로는 뷰모델 상태를 collectAsState()로 가져올 수 있음
    var nickname by remember { mutableStateOf("") }
    var nicknameError by remember { mutableStateOf("") }  // "이미 존재하는 닉네임입니다." 등
    var profileText by remember { mutableStateOf("자기소개입니다.자기소개입니다...") }
    var profileImageUrl by remember { mutableStateOf("https://via.placeholder.com/150") }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 이미지 표시 (예: Coil AsyncImage)
                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
                Spacer(modifier = Modifier.width(16.dp))
                // 이미지 수정 / 삭제 버튼
                Column {
                    Button(
                        onClick = {
                            // 이미지 수정 로직 (예: 갤러리 열기, 카메라, API 업로드 등)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("이미지 수정")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            // 이미지 삭제 로직
                            profileImageUrl = "" // 예시
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("이미지 삭제")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2) 닉네임 입력
            Text(text = "Nickname", style = Typography.titleMedium, color = customColors.Grey50)
            OutlinedTextField(
                value = nickname,
                onValueChange = { newValue ->
                    nickname = newValue
                    // 닉네임 유효성 검사 로직 (공백 사용 불가 등)
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
                onValueChange = { profileText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),  // 높이를 고정해서 여러 줄 표시
                maxLines = 5,
            )

            // 4) 하단 버튼 (취소, 수정)
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // 취소 버튼
                OutlinedButton(
                    onClick = {
                        navController.popBackStack()  // 이전 화면으로 돌아가기
                    }
                ) {
                    Text("취소")
                }
                Spacer(modifier = Modifier.width(16.dp))
                // 수정 버튼
                Button(
                    onClick = {
                        // 수정 API 호출 등
                        // 닉네임, 소개, 이미지 변경 등을 서버에 전송
                    }
                ) {
                    Text("수정")
                }
            }
        }
    }
}
