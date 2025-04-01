package com.whistlehub.common.view.signup

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.FilterChipDefaults.filterChipColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.whistlehub.common.data.remote.dto.response.AuthResponse
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.common.viewmodel.SignUpState
import com.whistlehub.common.viewmodel.SignUpViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SelectTagsScreen(
    userId: String,
    password: String,
    email: String,
    nickname: String,
    gender: Char,
    birth: String,
    onStartClick: (List<Int>) -> Unit = {},
    onBackClick: () -> Unit = {}, // 뒤로가기 기능을 위한 콜백 추가
) {
    val colors = CustomColors()
    val viewModel: SignUpViewModel = hiltViewModel()
    var apiTags by remember { mutableStateOf<List<AuthResponse.TagResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.getTagList { tagResponseList ->
            apiTags = tagResponseList.map { AuthResponse.TagResponse(it.id, it.name) }
            isLoading = false
        }
    }
    // 사용자가 선택한 태그 저장
    val selectedTags = remember { mutableStateListOf<Int>() }
    // 3개 이상 선택 여부에 따라 시작 버튼 활성/비활성
    val isStartEnabled = selectedTags.size >= 3


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "취향 태그 선택", style = Typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1C1C1C))
            )
        },
        content = { paddingValues ->
            // 기존 UI를 paddingValues로 감싸서 배치
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1C1C1C))
                    .padding(paddingValues)
                    .padding(30.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 상단 안내 문구
                    Text(
                        text = "3개 이상의 취향 태그를 선택해 주세요",
                        style = Typography.titleMedium.copy(color = Color.White),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "취향을 기반으로 다양한 트랙을 추천해드려요.",
                        style = Typography.bodyMedium.copy(color = Color.LightGray),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 태그 영역
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(8.dp)
                            .drawBehind {
                                val strokeWidth = 2.dp.toPx()
                                drawLine(
                                    color = Color.Gray,
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, 0f),
                                    strokeWidth = strokeWidth
                                )
                                drawLine(
                                    color = Color.Gray,
                                    start = Offset(0f, size.height),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = strokeWidth
                                )
                            }
                    ) {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            apiTags.forEach { tag ->
                                FilterChip(
                                    selected = selectedTags.contains(tag.id),
                                    onClick = {
                                        if (selectedTags.contains(tag.id)) {
                                            selectedTags.remove(tag.id)
                                        } else {
                                            selectedTags.add(tag.id)
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = tag.name,
                                            style = Typography.labelLarge.copy(
                                                color = if (selectedTags.contains(tag.id))
                                                    colors.Grey950
                                                else
                                                    Color.White
                                            )
                                        )
                                    },
                                    colors = filterChipColors(
                                        selectedContainerColor = colors.Mint500,
                                        containerColor = Color.DarkGray
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selectedTags.contains(tag.id),
                                        borderColor = if (selectedTags.contains(tag.id))
                                            colors.Mint500
                                        else
                                            Color.Gray,
                                        borderWidth = 1.dp
                                    )
                                )
                            }
                        }
                    }

                    // 하단 버튼
                    Button(
                        onClick = {
                            viewModel.registerAndAutoLogin(
                                loginId = userId,
                                password = password,
                                email = email,
                                nickname = nickname,
                                gender = gender,
                                birth = birth,
                                tagList = selectedTags.toList(),
                            ) {
                                onStartClick(selectedTags)
                            }
                        },
                        enabled = isStartEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isStartEnabled) colors.Mint500 else Color.Gray
                        )
                    ) {
                        Text(
                            text = "시작하기",
                            style = Typography.titleMedium.copy(color = Color.Black)
                        )
                    }
                }
            }
        }
    )
}
