package com.whistlehub.common.view.track

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography

@Composable
fun ReportDialog() {
    // 신고 다이얼로그 UI
    // 신고 사유는 하드코딩으로 설정
    val reportReasons = listOf("저작권 문제 위반 음원", "청소년에게 유해한 음원", "폭력적이거나 혐오스러운 음원", "스팸 또는 광고성 음원")
    var selectedReason by remember { mutableStateOf(reportReasons[0]) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        reportReasons.forEach { reason ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedReason = reason }
                    .background(if (selectedReason == reason) CustomColors().Grey600 else Color.Transparent, shape = RoundedCornerShape(10.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = reason, style = Typography.bodyLarge, color = CustomColors().Grey200)
            }
        }
    }
}