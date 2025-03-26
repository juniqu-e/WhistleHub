package com.whistlehub.common.view.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.FilterChipDefaults.filterChipColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagSelectionScreen(
    modifier: Modifier = Modifier,
    allTags: List<String> = listOf(
        "ROCK", "POP", "JAZZ", "EDM", "HIPHOP", "CLASSIC"
    ),
    onStartClick: (List<String>) -> Unit = {}
) {
    // 선택된 태그 저장
    val selectedTags = remember { mutableStateListOf<String>() }

    val colors = CustomColors()

    // 3개 이상 선택 여부에 따라 버튼 활성/비활성
    val isStartEnabled = selectedTags.size >= 3

    // 전체 배경
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1C)) // 짙은 배경색
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 상단 안내 문구
            Text(
                text = "3개 이상의 취향 태그를 선택해 주세요:",
                style = Typography.titleMedium.copy(color = Color.White),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "취향을 기반으로 다양한 트랙을 추천해드려요.",
                style = Typography.bodyMedium.copy(color = Color.LightGray),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 태그 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp)
                    .drawBehind {
                        // 테두리선(드로우 라인)
                        val strokeWidth = 2.dp.toPx()
                        drawLine(
                            color = Color.Gray,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = strokeWidth
                        )
                        drawLine(
                            color = Color.Gray,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = strokeWidth
                        )
                    }
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    allTags.forEach { tag ->
                        FilterChip(
                            selected = selectedTags.contains(tag),
                            onClick = {
                                if (selectedTags.contains(tag)) {
                                    selectedTags.remove(tag)
                                } else {
                                    selectedTags.add(tag)
                                }
                            },
                            label = {
                                Text(
                                    text = tag,
                                    style = Typography.labelLarge.copy(
                                        color = if (selectedTags.contains(tag))
                                            colors.Grey950
                                        else
                                            Color.White
                                    )
                                )
                            },
                            colors = filterChipColors(
                                selectedContainerColor = colors.Mint500,
                                containerColor = Color.DarkGray
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedTags.contains(tag),
                                borderColor = if (selectedTags.contains(tag))
                                    colors.Mint500
                                else
                                    Color.Gray,
                                borderWidth = 1.dp
                            )
                        )
                    }
                }
            }

            // 하단 버튼
            Button(
                onClick = { onStartClick(selectedTags.toList()) },
                enabled = isStartEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isStartEnabled) colors.Mint500 else Color.Gray
                )
            ) {
                Text(
                    text = "시작하기",
                    style = Typography.titleMedium.copy(color = Color.Black)
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
fun TagSelectionScreenPreview() {
    TagSelectionScreen()
}
