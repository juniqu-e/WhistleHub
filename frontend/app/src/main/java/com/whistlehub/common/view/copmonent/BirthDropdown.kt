package com.whistlehub.common.view.copmonent

import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.whistlehub.common.view.theme.CustomColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthDropdownFields(
    selectedYear: String,
    onYearSelected: (String) -> Unit,
    selectedMonth: String,
    onMonthSelected: (String) -> Unit,
    selectedDay: String,
    onDaySelected: (String) -> Unit,
    birthError: String?,
    labelStyle: TextStyle,
    textStyle: TextStyle,
    placeholderStyle: TextStyle
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 통합 라벨
        Text(text = "Birth", style = labelStyle)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 연도 드롭다운 (예: 1900 ~ 현재)
            BirthDropdownField(
                label = "Year",
                selectedOption = selectedYear,
                options = (1900..2023).map { it.toString().padStart(4, '0') },
                onOptionSelected = onYearSelected,
                textStyle = textStyle,
                placeholderStyle = placeholderStyle,
                modifier = Modifier.weight(1f)
            )
            // 월 드롭다운 (01 ~ 12)
            BirthDropdownField(
                label = "Month",
                selectedOption = selectedMonth,
                options = (1..12).map { it.toString().padStart(2, '0') },
                onOptionSelected = onMonthSelected,
                textStyle = textStyle,
                placeholderStyle = placeholderStyle,
                modifier = Modifier.weight(1f)
            )
            // 일 드롭다운 (01 ~ 31)
            BirthDropdownField(
                label = "Day",
                selectedOption = selectedDay,
                options = (1..31).map { it.toString().padStart(2, '0') },
                onOptionSelected = onDaySelected,
                textStyle = textStyle,
                placeholderStyle = placeholderStyle,
                modifier = Modifier.weight(1f)
            )
        }
        // 에러 메시지 영역 (고정 높이)
        Box(modifier = Modifier.height(30.dp)) {
            if (!birthError.isNullOrEmpty()) {
                Text(
                    text = birthError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BirthDropdownField(
    label: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    textStyle: TextStyle,
    placeholderStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    val colors = CustomColors()
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        BasicTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            textStyle = textStyle,
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .menuAnchor(),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            if (selectedOption.isEmpty()) {
                                Text(text = label, style = placeholderStyle)
                            }
                            innerTextField()
                        }
                        Icon(
                            imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            tint = colors.Mint500
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomStart)
                            .height(1.dp)
                            .background(colors.Grey50)
                    )
                }
            }
        )

        MaterialTheme(
            colorScheme = lightColorScheme(
                surface = colors.Grey50,
                onSurface = colors.Grey950
            )
        ) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option, style = textStyle.copy(color = colors.Grey950)) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
