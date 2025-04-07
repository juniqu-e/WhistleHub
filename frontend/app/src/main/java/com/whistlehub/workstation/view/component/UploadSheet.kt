package com.whistlehub.workstation.view.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.workstation.data.UploadMetadata

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadSheet(
    onDismiss: () -> Unit,
    onUploadClicked: (UploadMetadata) -> Unit,
    tagList: List<Pair<Int, String>>
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(true) } // 공개 여부
    var selectedTags by remember { mutableStateOf(setOf<Int>()) } // Tag
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val verticalScroll = rememberScrollState()
    val customColors = CustomColors()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(verticalScroll)
        ) {
            Text("믹스 업로드", style = Typography.titleLarge, color = customColors.CommonTextColor)

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = {
                    Text(
                        "제목",
                        style = Typography.bodyMedium,
                        color = CustomColors().CommonSubTextColor
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = {
                    Text(
                        "설명",
                        style = Typography.bodyMedium,
                        color = CustomColors().CommonSubTextColor
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("공개 여부", style = Typography.bodyLarge, color = customColors.CommonTextColor)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isPublic,
                    onClick = { isPublic = true }
                )
                Text("공개", modifier = Modifier.padding(end = 16.dp), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = !isPublic,
                    onClick = { isPublic = false }
                )
                Text("비공개", modifier = Modifier.padding(end = 16.dp), textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("태그", style = Typography.bodyLarge, color = customColors.CommonTextColor)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp) //
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(tagList) { (id, name) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedTags = if (selectedTags.contains(id)) {
                                        selectedTags - id
                                    } else {
                                        selectedTags + id
                                    }
                                }
                                .padding(8.dp)
                        ) {
                            Checkbox(
                                checked = selectedTags.contains(id),
                                onCheckedChange = {
                                    selectedTags = if (it) selectedTags + id else selectedTags - id
                                }
                            )
                            Text(
                                name,
                                style = Typography.labelLarge,
                                color = customColors.CommonSubTextColor
                            )
                        }
                    }
                }
            }

//            tagOptions.forEach { (id, name) ->
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clickable {
//                            selectedTags = if (selectedTags.contains(id)) {
//                                selectedTags - id
//                            } else {
//                                selectedTags + id
//                            }
//                        }
//                        .padding(vertical = 4.dp)
//                ) {
//                    Checkbox(
//                        checked = selectedTags.contains(id),
//                        onCheckedChange = {
//                            selectedTags = if (it) selectedTags + id else selectedTags - id
//                        }
//                    )
//                    Text(
//                        name,
//                        style = Typography.labelLarge,
//                        textAlign = TextAlign.Center,
//                        color = customColors.CommonSubTextColor
//                    )
//                }
//            }

            Button(
                onClick = {
                    onUploadClicked(
                        UploadMetadata(
                            title = title,
                            description = description,
                            visibility = if (isPublic) 1 else 0,
                            tags = selectedTags.toList()
                        )
                    )
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("업로드", style = Typography.bodyMedium, color = customColors.CommonTextColor)
            }
        }
    }
}
