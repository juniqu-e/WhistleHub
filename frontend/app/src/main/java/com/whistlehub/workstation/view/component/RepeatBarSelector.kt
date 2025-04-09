package com.whistlehub.workstation.view.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whistlehub.common.view.theme.CustomColors

@Composable
fun RepeatBarSelector(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    min: Int = 1,
    max: Int = 16,
    accentColor: Color = Color.Cyan
) {
    val customColors = CustomColors()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(vertical = 8.dp)
            .background(customColors.CommonSubTextColor.copy(0.4f), RoundedCornerShape(12.dp))
            .border(2.dp, accentColor.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        IconButton(onClick = { if (value > min) onValueChange(value - 1) }) {
            Icon(Icons.Default.Remove, contentDescription = "감소", tint = Color.White)
        }

        Text(
            text = "$label: $value",
            modifier = Modifier.padding(horizontal = 16.dp),
            color = Color.White,
            fontSize = 16.sp
        )

        IconButton(onClick = { if (value < max) onValueChange(value + 1) }) {
            Icon(Icons.Default.Add, contentDescription = "증가", tint = Color.White)
        }
    }
}
