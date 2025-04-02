package com.whistlehub.workstation.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.whistlehub.common.data.remote.dto.response.TrackResponse
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.search.view.SearchMode
import com.whistlehub.workstation.data.TrackSearchMode
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun SearchLayerSection(
    searchResults: List<TrackResponse.SearchTrack>,
    onSearchClicked: (String) -> Unit,
    onTrackSelected: (Int) -> Unit,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    var keyword by remember { mutableStateOf("") }
    var searchMode by remember { mutableStateOf(TrackSearchMode.DISCOVERY) }  // 기본 탐색 모드

    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "음원 검색",
                style = Typography.titleLarge,
                color = Color.Black,
            )

            Spacer(Modifier.height(12.dp))


            TextField(
                value = keyword,
                onValueChange = { keyword = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusEvent {
                        if (it.isFocused) {
                            searchMode = TrackSearchMode.SEARCHING
                        }
                    },
                singleLine = true,
                placeholder = {
                    Text(text = "검색어를 입력하세요.", style = Typography.bodyMedium)
                },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onSearchClicked(keyword)
                        searchMode = TrackSearchMode.COMPLETE_SEARCH
                        keyboardController?.hide()
                    }
                ),
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

                    unfocusedLabelColor = Color.LightGray,
                    focusedLabelColor = Color.Black,
                    cursorColor = Color.Black,
                ),
                shape = RoundedCornerShape(20.dp),
                textStyle = Typography.bodyMedium,
                trailingIcon = {
                    IconButton({
                        coroutineScope.launch {
                            onSearchClicked(keyword)
                            searchMode = TrackSearchMode.COMPLETE_SEARCH
                            keyboardController?.hide()
                        }
                    }) {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = "Search Icon"
                        )
                    }
                }
            )

            Spacer(Modifier.height(16.dp))

            when (searchMode.toString()) {
                "DISCOVERY" -> {

                }

                "SEARCHING" -> {

                }

                "COMPLETE_SEARCH" -> {
                    if (searchResults.isEmpty()) {
                        Text(
                            "검색 결과가 없습니다.",
                            color = Color.Gray,
                            style = Typography.bodyMedium
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            itemsIndexed(searchResults) { index, track ->
                                Button(
                                    onClick = {
                                        onTrackSelected(track.trackId)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(track.nickname)
                                }

                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("이전")
            }
        }
    }
}

