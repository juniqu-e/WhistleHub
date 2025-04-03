package com.whistlehub.common.view.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography

@Composable
fun CustomAlertDialog(
    showDialog: Boolean,
    title: String,
    message: String,
    confirmButtonText: String = "확인",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val customColors = CustomColors()

    if (showDialog) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1D1B20) // 어두운 배경색
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 제목
                    Text(
                        text = title,
                        style = Typography.titleLarge,
                        color = customColors.Grey50, // 밝은 텍스트 색상
                        textAlign = TextAlign.Center
                    )

                    // 메시지
                    Text(
                        text = message,
                        style = Typography.bodyLarge,
                        color = customColors.Grey200, // 약간 어두운 텍스트 색상
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 확인 버튼
                    Button(
                        onClick = {
                            onConfirm()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = customColors.Mint500 // 민트 색상 버튼
                        )
                    ) {
                        Text(
                            text = confirmButtonText,
                            style = Typography.titleMedium,
                            color = customColors.Grey950 // 버튼 텍스트 색상
                        )
                    }
                }
            }
        }
    }
}
