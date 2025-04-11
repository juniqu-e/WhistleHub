package com.whistlehub.workstation.view.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun MixProgressBar(
    modifier: Modifier = Modifier,
    progress: Float, // 0f to 1f
    backgroundColor: Color = Color(0xFF1E1E1E), // Dark background
    progressColor: Color = Color(0xFF8E2DE2), // Purple gradient
    height: Dp = 6.dp
) {
    Box(
        modifier = modifier
            .height(height)
            .background(backgroundColor, RoundedCornerShape(4.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = progress)
                .background(progressColor, RoundedCornerShape(4.dp))
        )
    }
}
