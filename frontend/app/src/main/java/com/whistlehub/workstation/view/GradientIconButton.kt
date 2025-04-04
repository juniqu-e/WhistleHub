package com.whistlehub.workstation.view

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun GradientIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    gradientColors: List<Color>,
    iconTint: Color = Color.White,
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .graphicsLayer {
                shadowElevation = 8.dp.toPx()
                shape = CircleShape
                clip = true
            }
            .background(
                brush = Brush.linearGradient(gradientColors),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ){
        Crossfade(targetState = icon, label = "IconFade") { iconToShow ->
            Icon(
                iconToShow,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

