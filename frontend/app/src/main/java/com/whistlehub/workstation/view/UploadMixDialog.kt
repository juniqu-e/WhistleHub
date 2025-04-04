package com.whistlehub.workstation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.whistlehub.common.view.theme.Typography
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun UploadMixDialog(
    title: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        )
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
        ) {
            Column(
                modifier = Modifier
                    .width(300.dp)
                    .wrapContentHeight()
                    .background(color = Color.White),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = title,
                    textAlign = TextAlign.Center,
                    style = Typography.titleLarge,
                    color = Color.Black,
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("트랙 이름") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(40.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(color = Color.LightGray)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                ) {
                    Button(
                        onClick = {
                            val uploadName =
                                text.ifBlank { generateDefaultMixFileName() }
                            onConfirm(uploadName)
                        },
                        shape = RectangleShape,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White, // 버튼 배경색상
                            contentColor = Color.Black, // 버튼 텍스트 색상
                            disabledContainerColor = Color.Gray, // 버튼 비활성화 배경 색상
                            disabledContentColor = Color.White, // 버튼 비활성화 텍스트 색상
                        )
                    ) {
                        Text(
                            text = "업로드",
                            textAlign = TextAlign.Center,
                            style = Typography.bodyLarge
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(color = Color.LightGray)
                    )

                    Button(
                        onClick = { onDismiss() },
                        shape = RectangleShape,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White, // 버튼 배경색상
                            contentColor = Color.Black, // 버튼 텍스트 색상
                            disabledContainerColor = Color.Gray, // 버튼 비활성화 배경 색상
                            disabledContentColor = Color.White, // 버튼 비활성화 텍스트 색상
                        )
                    ) {
                        Text(
                            text = "취소",
                            textAlign = TextAlign.Center,
                            style = Typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

fun generateDefaultMixFileName(): String {
    val formatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    val timestamp = formatter.format(Date())
    return "mix_$timestamp.wav"
}