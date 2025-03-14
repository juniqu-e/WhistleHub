package com.whistlehub.common.view.typography

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.whistlehub.common.view.theme.Typography

class Pretendard {
    @Composable
    fun TitleLarge(
        text: String,
        modifier: Modifier,
        style: TextStyle = Typography.titleLarge
    ) {
        Text(
            text = text,
            modifier = modifier,
            style = style
        )
    }
}