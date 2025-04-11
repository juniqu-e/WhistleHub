package com.whistlehub.search.view

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.whistlehub.common.util.LogoutManager
import com.whistlehub.common.view.component.CommonAppBar
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.common.view.track.TrackItemRow
import com.whistlehub.playlist.data.TrackEssential
import com.whistlehub.playlist.viewmodel.TrackPlayViewModel
import com.whistlehub.search.view.discovery.DiscoveryView
import com.whistlehub.search.viewmodel.SearchViewModel
import com.whistlehub.workstation.viewmodel.WorkStationViewModel
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    paddingValues: PaddingValues,
    navController: NavHostController,
    trackPlayViewModel: TrackPlayViewModel = hiltViewModel(),
    searchViewModel: SearchViewModel,
    workStationViewModel: WorkStationViewModel,
    logoutManager: LogoutManager,
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var isFocused by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchMode by remember { mutableStateOf(SearchMode.DISCOVERY) }  // 기본 탐색 모드
    val searchResult by searchViewModel.searchResult.collectAsState()
    val tagList by searchViewModel.tagList.collectAsState()
    val customColors = CustomColors()

    LaunchedEffect(Unit) {
        searchViewModel.recommendTag()
    }

    Scaffold(
        topBar = {
            CommonAppBar(
                title = "Discovery",
                navController = navController,
                logoutManager = logoutManager,
                coroutineScope = coroutineScope
            )
        }
    ) { innerPadding ->
        Column(Modifier
            .padding(innerPadding)
            .fillMaxSize()
        ) {
            // 검색창
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { newQuery ->
                    searchQuery = newQuery
                },
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { state ->
                        isFocused = state.isFocused
                    },
                placeholder = { Text(
                    text = "Search Track",
                    style = Typography.bodyMedium,
                ) },
                trailingIcon = {
                    Row {
                        if (isFocused && searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    searchQuery = ""
                                }
                            ) {
                                Icon(
                                    Icons.Rounded.Cancel,
                                    contentDescription = "Clear Search Icon"
                                )
                            }
                        }
                        IconButton({
                            coroutineScope.launch {
                                searchViewModel.searchTracks(searchQuery)
                                searchMode = SearchMode.COMPLETE_SEARCH
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        }) {
                            Icon(
                                Icons.Rounded.Search,
                                contentDescription = "Search Icon"
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
            when (searchMode) {
                SearchMode.DISCOVERY -> {
                    Column(Modifier.weight(1f)) {
                        DiscoveryView(
                            Modifier.weight(1f),
                            tagList,
                            navController = navController,
                            searchViewModel = searchViewModel,
                            paddingValues = paddingValues
                        )
                    }
                }

                SearchMode.SEARCHING -> {}
                SearchMode.COMPLETE_SEARCH -> {
                    if (searchResult.isEmpty()) {
                        // 검색 결과가 없을 때 보여주는 UI
                        Text(
                            text = "검색 결과가 없습니다.",
                            modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth(),
                            style = Typography.bodyMedium,
                            color = CustomColors().Grey400,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // 검색 결과를 보여주는 UI
                        LazyColumn(Modifier.weight(1f)) {
                            items(searchResult.size) { index ->
                                val track = TrackEssential(
                                    trackId = searchResult[index].trackId,
                                    title = searchResult[index].title,
                                    artist = searchResult[index].nickname,
                                    imageUrl = searchResult[index].imageUrl,
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    TrackItemRow(track,
                                        trackPlayViewModel = trackPlayViewModel,
                                        workStationViewModel = workStationViewModel,
                                        navController = navController,
                                        needImportButton = true
                                    )
                                }
                            }
                            item {
                                Spacer(Modifier.height(paddingValues.calculateBottomPadding()))
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class SearchMode {
    DISCOVERY,
    SEARCHING,
    COMPLETE_SEARCH,
}