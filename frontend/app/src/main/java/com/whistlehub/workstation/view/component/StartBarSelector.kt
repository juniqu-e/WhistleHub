package com.whistlehub.workstation.view.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography

@Composable
fun StartBarSelector(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    min: Int = 1,
    max: Int = 64,
    accentColor: Color = Color.Cyan
) {
    val customColors = CustomColors()
    val glassColor = try {
        accentColor.copy(alpha = 0.3f)
    } catch (e: Exception) {
        Color(0xFFBDBDBD).copy(alpha = 0.7f) // fallback color
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        glassColor,
                        Color.White.copy(alpha = 0.09f)
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.7f),
                        glassColor,
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        IconButton(onClick = { if (value > min) onValueChange(value - 1) }) {
            Icon(Icons.Default.Remove, contentDescription = "감소", tint = Color.White)
        }

        Text(
            text = "$label: ${value.toInt() + 1}",
            modifier = Modifier.padding(horizontal = 16.dp),
            color = Color.White,
            style = Typography.bodyLarge
        )

        IconButton(onClick = { if (value < max) onValueChange(value + 1) }) {
            Icon(Icons.Default.Add, contentDescription = "증가", tint = Color.White)
        }
    }
}
