package com.whistlehub.playlist.view.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography

@Preview
@Composable
fun CreatePlaylist(
    onInputTitle : (String) -> Unit = {},
    onInputDescription : (String) -> Unit = {},
) {
    var playlistTitle by remember { mutableStateOf("") }
    var playlistDescription by remember { mutableStateOf("") }
//    val radioOptions = listOf("Private", "Public")
//    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        // Image
//        ImageUpload {}

        // Title
        Row(
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth()
                .border(1.dp, CustomColors().Grey50, RoundedCornerShape(5.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextField(
                value = playlistTitle,
                onValueChange = {
                    playlistTitle = it
                    onInputTitle(it) },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "Playlist Title",
                        color = CustomColors().Grey300,
                        style = Typography.bodyMedium,
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                ),
                shape = RoundedCornerShape(10.dp),
                textStyle = Typography.bodyMedium.copy(color = CustomColors().Grey50),
                singleLine = true,
            )
        }

        // Description
        Row(
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth()
                .border(1.dp, CustomColors().Grey50, RoundedCornerShape(5.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextField(
                value = playlistDescription,
                onValueChange = { playlistDescription = it
                                onInputDescription(it) },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "Playlist description",
                        color = CustomColors().Grey300,
                        style = Typography.bodyMedium,
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                ),
                shape = RoundedCornerShape(10.dp),
                textStyle = Typography.bodyMedium.copy(color = CustomColors().Grey50),
                maxLines = 5,
                minLines = 5
            )
        }

        // Private-Public
//        Row(Modifier.selectableGroup()) {
//            radioOptions.forEach { text ->
//                Row(
//                    Modifier
//                        .selectable(
//                            selected = (text == selectedOption),
//                            onClick = { onOptionSelected(text) },
//                            role = Role.RadioButton
//                        )
//                        .padding(10.dp),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(10.dp)
//                ) {
//                    RadioButton(
//                        selected = (text == selectedOption),
//                        onClick = null, // null recommended for accessibility with screen readers
//                        colors = RadioButtonDefaults.colors(
//                            selectedColor = CustomColors().Mint500,
//                            unselectedColor = CustomColors().Mint500
//                        )
//                    )
//                    Text(
//                        text = text,
//                        style = Typography.bodyMedium,
//                        color = CustomColors().Grey50
//                    )
//                }
//            }
//        }
    }
}