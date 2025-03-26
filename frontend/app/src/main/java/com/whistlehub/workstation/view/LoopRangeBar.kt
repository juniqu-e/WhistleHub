package com.whistlehub.workstation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoopRangeBar(
    loopStart: Int,
    loopEnd: Int,
    onRangeChange: (Int, Int) -> Unit
) {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        for (i in 0 until 60) {
            val isInRange = i in loopStart..loopEnd
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(20.dp)
                    .padding(1.dp)
                    .background(if (isInRange) Color.Green else Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${i + 1}",
                    fontSize = 10.sp,
                    color = Color.White,
                    modifier = Modifier.clickable {
                        // 클릭 시 루프 범위 새로 지정
                        onRangeChange(i, (i + 7).coerceAtMost(59))
                    }
                )
            }
        }
    }
}
