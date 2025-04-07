package com.whistlehub.profile.view.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.whistlehub.R
import com.whistlehub.common.data.remote.dto.response.ProfileResponse
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.profile.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction

/**
 * 유저 검색 기능을 제공하는 검색 바 컴포넌트
 */
@Composable
fun ProfileSearchBar(
    viewModel: ProfileViewModel,
    onUserSelected: (Int) -> Unit
) {
    val customColors = CustomColors()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // 포커스 요청자
    val focusRequester = remember { FocusRequester() }

    // 상태 관리
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }

    // ViewModel에서 검색 결과 가져오기
    val searchResults by viewModel.searchResults.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearSearchResults()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 검색 텍스트 필드
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { newQuery ->
                searchQuery = newQuery

                coroutineScope.launch {
                    if (newQuery.isEmpty()) {
                        viewModel.clearSearchResults()
                        isSearching = false
                    } else {
                        isSearching = true
                        // 검색 시작 전에 결과 초기화
                        viewModel.clearSearchResults()

                        try {
                            viewModel.searchProfiles(newQuery)
                        } catch (e: Exception) {
                            // 에러가 발생해도 별도 처리 없이 결과만 비움
                            viewModel.clearSearchResults()
                        } finally {
                            isSearching = false
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { state ->
                    isFocused = state.isFocused
                },
            placeholder = { Text("유저 검색", style = Typography.bodyMedium) },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = customColors.Grey200
                )
            },
            trailingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = customColors.Mint500,
                        strokeWidth = 2.dp
                    )
                } else if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            searchQuery = ""
                            viewModel.clearSearchResults()
                        }
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = customColors.Grey200
                        )
                    }
                }
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
            textStyle = Typography.bodyMedium,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            )
        )

        // 결과 표시: 포커스가 있고 검색어가 있을 때만
        if (isFocused && searchQuery.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                color = customColors.Grey800,
                shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
            ) {
                when {
                    // 검색 중일 때
                    isSearching -> {
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
                    }

                    // 검색 결과가 있을 때
                    searchResults.isNotEmpty() -> {
                        LazyColumn {
                            items(searchResults.size) { index ->
                                val profile = searchResults[index]
                                SearchResultItem(
                                    profile = profile,
                                    isFollowed = viewModel.isUserFollowed(profile.memberId),
                                    onClick = {
                                        searchQuery = ""
                                        viewModel.clearSearchResults()
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        onUserSelected(profile.memberId)
                                    }
                                )
                            }
                        }
                    }

                    // 검색 결과가 없을 때 (에러 포함)
                    else -> {
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
                    }
                }
            }
        }
    }

    BackHandler(enabled = isFocused) {
        keyboardController?.hide()
        focusManager.clearFocus()
        isFocused = false
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