package com.whistlehub.common.view.signup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.whistlehub.common.view.theme.CustomColors

/**
 * GenderSelection: 성별 입력을 위한 RadioButton 그룹 (선택된 항목은 민트색)
 */
@Composable
fun GenderSelection(
    selectedGender: String,
    onGenderSelected: (String) -> Unit,
    labelStyle: TextStyle,
    optionTextStyle: TextStyle,
    colors: CustomColors,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Gender",
            style = labelStyle
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedGender == "남성",
                onClick = { onGenderSelected("남성") },
                colors = RadioButtonDefaults.colors(
                    selectedColor = colors.Mint500,
                    unselectedColor = Color.White
                )
            )
            Text(
                text = "남성",
                style = optionTextStyle,
                modifier = Modifier.padding(end = 16.dp)
            )
            RadioButton(
                selected = selectedGender == "여성",
                onClick = { onGenderSelected("여성") },
                colors = RadioButtonDefaults.colors(
                    selectedColor = colors.Mint500,
                    unselectedColor = Color.White
                )
            )
            Text(
                text = "여성",
                style = optionTextStyle
            )
        }
    }
}