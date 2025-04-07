package com.whistlehub.workstation.view.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whistlehub.workstation.data.UploadMetadata

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadSheet(
    onDismiss: () -> Unit,
    onUploadClicked: (UploadMetadata) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(true) } // 공개 여부
    var selectedTags by remember { mutableStateOf(setOf<Int>()) } // Tag
    val tagOptions = listOf(
        0 to "Record",
        1 to "Whistle",
        2 to "Acoustic Guitar",
        3 to "Voice",
        4 to "Drums",
        5 to "Bass",
        6 to "Electric Guitar",
        7 to "Piano",
        8 to "Synth",
    )

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("믹스 업로드", fontSize = 20.sp)

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("제목") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("설명") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text("공개 여부")
            Row {
                RadioButton(
                    selected = isPublic,
                    onClick = { isPublic = true }
                )
                Text("공개", modifier = Modifier.padding(end = 16.dp))
                RadioButton(
                    selected = !isPublic,
                    onClick = { isPublic = false }
                )
                Text("비공개")
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("태그 선택")
            tagOptions.forEach { (id, name) ->
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
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = selectedTags.contains(id),
                        onCheckedChange = {
                            selectedTags = if (it) selectedTags + id else selectedTags - id
                        }
                    )
                    Text(name)
                }
            }

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
                Text("업로드")
            }
        }
    }
}
