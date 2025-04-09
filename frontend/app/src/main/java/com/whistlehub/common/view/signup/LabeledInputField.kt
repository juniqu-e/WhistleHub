package com.whistlehub.common.view.signup

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography

@Composable
fun LabeledInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    labelStyle: TextStyle,
    textStyle: TextStyle,
    placeholderStyle: TextStyle,
    isFocused: Boolean,
    onFocusChange: (Boolean) -> Unit,
    errorMessage: String? = null,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    maxLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val colors = CustomColors()

    Column(modifier = modifier.fillMaxWidth()) {
        // Box: 라벨 + 입력 필드 + drawBehind 라인
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .drawBehind {
                    val strokeWidth = 1.dp.toPx()
                    val offset = 4.dp.toPx() // 1픽셀 만큼 아래로 이동
                    val y = size.height - strokeWidth + offset
                    drawLine(
                        color = if (isFocused) colors.CommonFocusColor else Color.White.copy(alpha = 0.7f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokeWidth
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart)
            ) {
                // 라벨
                Text(
                    text = label,
                    style = labelStyle.copy(color = if (isFocused) colors.CommonFocusColor else labelStyle.color)
                )
                Spacer(modifier = Modifier.height(12.dp))
                // 입력 필드
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = textStyle,
                    keyboardOptions = keyboardOptions,
                    visualTransformation = visualTransformation,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { onFocusChange(it.isFocused) },
                    interactionSource = remember { MutableInteractionSource() },
                    decorationBox = { innerTextField ->
                        Box {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = placeholderStyle
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
        // 에러 메시지 영역
        Box(modifier = Modifier.height(30.dp)) {
            if (!errorMessage.isNullOrEmpty()) {
                val displayColor = if (
                    errorMessage.contains("사용 가능한") ||
                    errorMessage.contains("인증 성공") ||
                    errorMessage.contains("요청이 성공적으로 처리되었습니다.") ||
                    errorMessage.contains("인증 코드가 전송되었습니다.")
                ) Color.Green
                                    else MaterialTheme.colorScheme.error

                Text(
                    text = errorMessage,
                    color = displayColor,
                    style = Typography.labelSmall,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }
        }
    }
}