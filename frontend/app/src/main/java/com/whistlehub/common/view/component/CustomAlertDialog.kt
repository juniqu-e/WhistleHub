package com.whistlehub.common.view.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun CustomAlertDialog(
    showDialog: Boolean,
    title: String,
    message: String,
    confirmButtonText: String = "확인",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
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
                    containerColor = Color(0xFF121212) // 어두운 배경색
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
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFFE0E0E0), // 밝은 텍스트 색상
                        textAlign = TextAlign.Center
                    )

                    // 메시지
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFBDBDBD), // 약간 어두운 텍스트 색상
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
                            containerColor = Color(0xFF00BFA5) // 민트 색상 버튼
                        )
                    ) {
                        Text(
                            text = confirmButtonText,
                            color = Color(0xFF000000) // 버튼 텍스트 색상
                        )
                    }
                }
            }
        }
    }
}
