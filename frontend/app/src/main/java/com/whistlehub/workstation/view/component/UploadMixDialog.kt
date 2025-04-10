package com.whistlehub.workstation.view.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Pretendard
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.workstation.data.UploadMetadata


@Composable
fun UploadDialog(
    onDismiss: () -> Unit,
    onUploadClicked: (UploadMetadata) -> Unit,
    tagList: List<Pair<Int, String>>
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(true) }
    var selectedTags by remember { mutableStateOf(setOf<Int>()) }
    val verticalScroll = rememberScrollState()
    val customColors = CustomColors()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .heightIn(max = 600.dp)
                .shadow(8.dp, shape = RoundedCornerShape(8.dp)) // 그림자 추가
                .background(customColors.CommonBackgroundColor) // 배경 색상
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(verticalScroll)
            ) {
                TitleText("믹스 업로드")

                Spacer(modifier = Modifier.height(12.dp))
                InputField("제목", title) { title = it }
                InputField("설명", description) { description = it }

                Spacer(modifier = Modifier.height(16.dp))
                Text("공개 여부", style = Typography.titleLarge, color = customColors.CommonTextColor)
                PrivacySelection(isPublic) { isPublic = it }

                Spacer(modifier = Modifier.height(16.dp))
                TagSelection(tagList, selectedTags) { selectedTags = it }
                Spacer(modifier = Modifier.height(16.dp))

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
                    modifier = Modifier
                        .align(Alignment.End)
                        .fillMaxWidth()
                        .padding(4.dp) // 여백을 추가하여 버튼을 더 보기 좋게
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    customColors.CommonButtonColor,
                                    customColors.CommonButtonColor,
                                )
                            )
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(0.66f),
                                    Color.White.copy(0.52f)
                                )
                            ),
                            shape = RoundedCornerShape(40.dp)
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = customColors.CommonButtonColor.copy(0.1f), // 기본 색상
                        contentColor = customColors.CommonTextColor.copy(0.3f) // 버튼 텍스트 색상
                    ),
                ) {
                    Text(
                        "업로드",
                        style = Typography.titleMedium,
                        color = customColors.CommonTextColor
                    )
                }
            }
        }
    }
}

@Composable
fun TitleText(title: String) {
    Text(
        title,
        style = Typography.headlineMedium,
        fontFamily = Pretendard,
        fontWeight = FontWeight.Bold,
        color = CustomColors().CommonTextColor
    )
}

@Composable
fun InputField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                label,
                style = Typography.bodyMedium,
                color = CustomColors().CommonSubTextColor
            )
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun PrivacySelection(isPublic: Boolean, onPrivacyChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = isPublic, onClick = { onPrivacyChange(true) })
        Text("공개", modifier = Modifier.padding(end = 16.dp), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.width(16.dp))
        RadioButton(selected = !isPublic, onClick = { onPrivacyChange(false) })
        Text("비공개", modifier = Modifier.padding(end = 16.dp), textAlign = TextAlign.Center)
    }
}

@Composable
fun TagCheckbox(
    tagName: String,
    isSelected: Boolean,
    onSelected: (Boolean) -> Unit,
    index: Int
) {
    val customColors = CustomColors()
    val tagColors = listOf(
        Color(0xFFFFC107),  // Yellow
        Color(0xFF2196F3),  // Blue
        Color(0xFF4CAF50),  // Green
        Color(0xFFF44336),  // Red
        Color(0xFF9C27B0),  // Purple
        Color(0xFFFF9800),  // Orange
        Color(0xFF8BC34A),  // Light Green
        Color(0xFF3F51B5),  // Indigo
        Color(0xFF009688),  // Teal
        Color(0xFF673AB7),  // Deep Purple
        Color(0xFFE91E63),  // Pink
        Color(0xFF00BCD4),  // Cyan
        Color(0xFF795548),  // Brown
        Color(0xFF607D8B),  // Blue Grey
        Color(0xFF512DA8),  // Deep Purple A200
        Color(0xFFFF5722),  // Deep Orange
        Color(0xFFCDDC39),  // Lime
        Color(0xFF8D6E63),  // Brown
        Color(0xFF388E3C),  // Dark Green
        Color(0xFF0288D1),  // Light Blue
        Color(0xFFDD2C00),  // Red (Dark)
        Color(0xFF00796B),  // Teal (Dark)
        Color(0xFF9E9D24)   // Yellow Green
    )
    val tagColor = tagColors[index % tagColors.size]
    val tagBackgroundColor =
        if (isSelected) tagColor.copy(alpha = 0.7f) else tagColor.copy(alpha = 0.3f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected(!isSelected) }
            .padding(8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        tagBackgroundColor,
                        Color.White.copy(alpha = 0.03f)
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.7f),
                        tagBackgroundColor,
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            ),
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onSelected(it) },
            colors = CheckboxDefaults.colors(
                checkedColor = customColors.CommonBackgroundColor.copy(0.54f),
                uncheckedColor = Color.Black.copy(0.44f)
            )
        )
        Text(
            text = tagName,
            style = Typography.bodyMedium,
            color = customColors.CommonTextColor,
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}

@Composable
fun TagSelection(
    tagList: List<Pair<Int, String>>,
    selectedTags: Set<Int>,
    onTagsChanged: (Set<Int>) -> Unit
) {
    val customColors = CustomColors()
    Text("태그", style = Typography.titleLarge, color = customColors.CommonTextColor)
    Spacer(modifier = Modifier.height(16.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 300.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize()
        ) {
            items(tagList) { (id, name) ->
                TagCheckbox(
                    tagName = name,
                    isSelected = selectedTags.contains(id),
                    onSelected = { isSelected ->
                        onTagsChanged(
                            if (isSelected) selectedTags + id else selectedTags - id
                        )
                    },
                    index = id // 색상 인덱스
                )
            }
        }
    }
}
