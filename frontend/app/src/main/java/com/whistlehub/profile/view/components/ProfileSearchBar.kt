package com.whistlehub.profile.view.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.whistlehub.R
import com.whistlehub.common.data.remote.dto.response.ProfileResponse
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.profile.viewmodel.ProfileViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 유저 검색 기능을 제공하는 검색 바 컴포넌트
 */
@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSearchBar(
    viewModel: ProfileViewModel,
    onUserSelected: (Int) -> Unit
) {
    val customColors = CustomColors()
    val coroutineScope = rememberCoroutineScope()
    val localDensity = LocalDensity.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var searchQuery by remember { mutableStateOf("") }
    var isSearchFocused by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var textFieldWidth by remember { mutableStateOf(0.dp) }
    var searchJob by remember { mutableStateOf<Job?>(null) }
    var dropdownVisible by remember { mutableStateOf(false) }

    val searchResults = viewModel.searchResults.value

    // 컴포넌트가 dispose될 때 검색 결과 초기화
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearSearchResults()
        }
    }

    // 포커스 변경에 따른 처리
    LaunchedEffect(isSearchFocused) {
        dropdownVisible = isSearchFocused

        if (isSearchFocused && searchQuery.isNotEmpty()) {
            // 포커스 얻었을 때 이미 검색어가 있으면 검색 시작
            isSearching = true
            viewModel.searchProfiles(searchQuery)
            isSearching = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 검색 텍스트 필드
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { newQuery ->
                        searchQuery = newQuery

                        // 기존 검색 작업을 취소
                        searchJob?.cancel()

                    // 디바운싱 적용 검색
                    searchJob = coroutineScope.launch {

                        isSearching = true
                        viewModel.searchProfiles(newQuery)
                        isSearching = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onGloballyPositioned { coordinates ->
                        textFieldWidth = with(localDensity) { coordinates.size.width.toDp() }
                    }
                    .onFocusChanged { focusState ->
                        isSearchFocused = focusState.isFocused
                    }
                    .zIndex(1f),
                placeholder = { Text("유저 검색", style = Typography.bodyMedium) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = customColors.Grey200
                    )
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = customColors.Grey50,
                    unfocusedTextColor = customColors.Grey50,
                    focusedPlaceholderColor = customColors.Grey200,
                    unfocusedPlaceholderColor = customColors.Grey200,
                    cursorColor = customColors.Mint500,
                    focusedIndicatorColor = customColors.Mint500,
                    unfocusedIndicatorColor = customColors.Grey200,
                    focusedContainerColor = customColors.Grey700.copy(alpha = 0.5f),
                    unfocusedContainerColor = customColors.Grey700.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp),
                textStyle = Typography.bodyMedium
            )

            // 커스텀 드롭다운 (Popup 사용)
            if (dropdownVisible) {
                // 키보드 입력을 방해하지 않는 Popup 사용
                Popup(
                    alignment = Alignment.TopCenter,
                    onDismissRequest = {
                        // 팝업 외부 클릭 시 포커스 해제
                        dropdownVisible = false
                        isSearchFocused = false
                        focusManager.clearFocus()
                    },
                    properties = PopupProperties(
                        focusable = false, // 중요: 포커스 가능 false로 설정하여 키보드 유지
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true
                    )
                ) {
                    Surface(
                        modifier = Modifier
                            .width(textFieldWidth)
                            .padding(top = 60.dp) // 텍스트 필드 아래에 위치
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = false) { /* 팝업 내부 클릭 시 이벤트 소비 */ },
                        color = customColors.Grey800,
                        shadowElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                                .verticalScroll(scrollState)
                                .padding(vertical = 8.dp)
                        ) {
                            if (isSearching) {
                                // 검색 중 로딩 표시
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = customColors.Mint500,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            } else if (searchQuery.isEmpty()) {
                                // 검색어가 없을 때
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "검색어를 입력하세요",
                                        style = Typography.bodyMedium,
                                        color = customColors.Grey300
                                    )
                                }
                            } else if (searchResults.isEmpty()) {
                                // 검색 결과가 없을 때
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "검색 결과가 없습니다",
                                        style = Typography.bodyMedium,
                                        color = customColors.Grey300
                                    )
                                }
                            } else {
                                // 검색 결과 표시
                                searchResults.forEach { profile ->
                                    SearchResultItem(
                                        profile = profile,
                                        isFollowed = viewModel.isUserFollowed(profile.memberId),
                                        onClick = {
                                            searchQuery = ""
                                            dropdownVisible = false
                                            isSearchFocused = false
                                            focusManager.clearFocus() // 포커스 명시적 해제
                                            onUserSelected(profile.memberId)
                                        }
                                    )
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
 * 검색 결과의 개별 아이템을 표시하는 컴포넌트
 */
@Composable
fun SearchResultItem(
    profile: ProfileResponse.SearchProfileResponse,
    isFollowed: Boolean,
    onClick: () -> Unit
) {
    val customColors = CustomColors()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = profile.profileImage ?: R.drawable.default_profile,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )

        Text(
            text = profile.nickname,
            style = Typography.bodyLarge,
            color = customColors.Grey50,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        )

        // 팔로우 중인 유저에 표시되는 아이콘
        if (isFollowed) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = customColors.Mint500,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "팔로잉",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}