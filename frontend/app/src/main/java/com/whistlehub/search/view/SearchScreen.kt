package com.whistlehub.search.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography

@Composable
fun SearchScreen(){
    var searchText by remember { mutableStateOf("") }

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
        TextField(
            value = searchText,
            onValueChange = { searchText = it},
            placeholder = {
                Text(
                    text = "Search Track",
                    style = Typography.bodyMedium,
                )
            },
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
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
                IconButton({}) {
                    Icon(Icons.Rounded.Search,
                        contentDescription = "Search Icon"
                    )
                }
            }
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp)
        ) {
            items(tags.size) { index ->
                val tag = tags[index]
                Box(Modifier
                    .padding(10.dp)
                    .background(CustomColors().Mint500, RoundedCornerShape(10.dp))
                    .padding(top = 70.dp, bottom = 10.dp, start = 10.dp, end = 10.dp)
                    .clickable {},
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Text(
                        text = "#$tag",
                        modifier = Modifier
                            .padding(5.dp),
                        style = Typography.bodyMedium,
                        color = CustomColors().Grey950
                    )
                }
            }
        }
    }
}