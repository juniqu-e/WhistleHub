package com.whistlehub.workstation.view.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.workstation.data.PatternBlock

@Composable
fun BeatGrid(
    patternBlocks: List<PatternBlock>,
    onClick: (Int) -> Unit
) {
    val blockMap = mutableMapOf<Int, Boolean>().apply {
        patternBlocks.forEach { block ->
            for (i in block.start until (block.start + block.length)) {
                this[i] = (i == block.start)
            }
        }
    }

    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
        for (i in 0 until 64) {
            val isStart = blockMap[i] == true
            val isBlocked = blockMap.containsKey(i)

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .padding(2.dp)
                    .background(
                        when {
                            isStart -> CustomColors().Mint500
                            isBlocked -> CustomColors().Mint300
                            else -> Color.DarkGray
                        }
                    )
                    .clickable { onClick(i) }, // 완전 자유 클릭
                contentAlignment = Alignment.Center
            ) {
                Text("${i + 1}", fontSize = 10.sp, color = Color.Black)
            }
        }
    }
//    Row(
//        modifier = Modifier
//            .horizontalScroll(rememberScrollState())
//            .padding(8.dp)
//    ) {
//        for (i in pattern.indices) {
//            val isStart = pattern[i] && (i == 0 || !pattern[i - 1])
//            val isBlocked = pattern[i]
//            val boxColor = when {
//                isStart -> Color.Cyan // 시작 칸
//                isBlocked -> Color.LightGray // 이어진 칸
//                else -> Color.DarkGray
//            }
//
//            Box(
//                modifier = Modifier
//                    .width(40.dp)
//                    .height(60.dp)
//                    .padding(2.dp)
//                    .background(boxColor, RoundedCornerShape(4.dp))
//                    .clickable(enabled = isStart || !isBlocked) {
//                        onClick(i)
//                    },
//                contentAlignment = Alignment.Center
//            ) {
//                Text(text = "${i + 1}", fontSize = 10.sp, color = Color.White)
//            }
//        }
//    }
}
