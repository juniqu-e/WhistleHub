package com.whistlehub.search.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.search.view.discovery.DiscoveryView
import com.whistlehub.search.viewmodel.SearchViewModel
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    navController: NavHostController,
    searchViewModel: SearchViewModel = hiltViewModel(),
) {
    val coroutineScope = rememberCoroutineScope()
    var searchText by remember { mutableStateOf("") }
    var searchMode by remember { mutableStateOf(SearchMode.DISCOVERY) }  // 기본 탐색 모드
    val searchResult by searchViewModel.searchResult.collectAsState()

    // 임시 추천 태그
    val tags = listOf(
        "Pop",
        "Rock",
        "Hip-Hop",
        "R&B",
        "Jazz",
        "Classical",
        "Electronic",
        "Reggae",
        "Country",
        "Folk"
    )

    Column(Modifier.fillMaxWidth()) {
        // 검색창
        TextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = {
                Text(
                    text = "Search Track",
                    style = Typography.bodyMedium,
                )
            },
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .onFocusEvent {
                    if (it.isFocused) {
                        searchMode = SearchMode.SEARCHING
                    }
                },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedContainerColor = CustomColors().Grey200,
                unfocusedContainerColor = CustomColors().Grey200,
                unfocusedPlaceholderColor = CustomColors().Grey700,
                unfocusedTrailingIconColor = CustomColors().Grey950,
                focusedTrailingIconColor = CustomColors().Grey950,
                unfocusedTextColor = CustomColors().Grey950,
                focusedTextColor = CustomColors().Grey950,
            ),
            shape = RoundedCornerShape(20.dp),
            textStyle = Typography.bodyMedium,
            singleLine = true,
            trailingIcon = {
                IconButton({
                    coroutineScope.launch {
                        searchViewModel.searchTracks(searchText)
                        searchMode = SearchMode.COMPLETE_SEARCH
                    }
                }) {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = "Search Icon"
                    )
                }
            },
        )
        when (searchMode) {
            SearchMode.DISCOVERY -> {
                DiscoveryView(tags, navController = navController)
            }

            SearchMode.SEARCHING -> {}
            SearchMode.COMPLETE_SEARCH -> {
                if (searchResult.isEmpty()) {
                    // 검색 결과가 없을 때 보여주는 UI
                    Text(
                        text = "검색 결과가 없습니다.",
                        modifier = Modifier.padding(10.dp),
                        style = Typography.bodyMedium,
                        color = CustomColors().Grey700
                    )
                } else {
                    // 검색 결과를 보여주는 UI
                    // 임시 UI
                    LazyColumn {
                        items(searchResult.size) { index ->
                            val track = searchResult[index]
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = track.imageUrl,
                                    contentDescription = "Track Cover",
                                    modifier = Modifier
                                        .size(50.dp)
                                        .padding(10.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                )
                                Text(
                                    text = track.title,
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .fillMaxWidth(0.8f),
                                    style = Typography.bodyMedium,
                                    color = CustomColors().Grey50
                                )
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