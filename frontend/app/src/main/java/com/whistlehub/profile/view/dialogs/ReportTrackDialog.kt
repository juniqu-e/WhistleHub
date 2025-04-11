package com.whistlehub.profile.view.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography

/**
 * Dialog for reporting a track
 *
 * @param onDismiss Called when user dismisses the dialog
 * @param onSubmit Called with the report reason when user submits
 */
@Composable
fun ReportTrackDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    val customColors = CustomColors()
    var reportReason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("트랙 신고하기", color = customColors.Error700) },
        text = {
            Column {
                Text(
                    "신고 사유를 입력하세요",
                    color = customColors.Grey200
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = reportReason,
                    onValueChange = { reportReason = it },
                    placeholder = { Text("신고 사유를 입력하세요", color = customColors.Grey400) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = customColors.CommonFocusColor,
                        unfocusedBorderColor = customColors.Grey700,
                        focusedTextColor = customColors.Grey50,
                        unfocusedTextColor = customColors.Grey50
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(reportReason) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = customColors.Error700,
                    contentColor = customColors.Grey50
                )
            ) {
                Text("제출하기")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = customColors.Grey300
                )
            ) {
                Text("취소")
            }
        },
        containerColor = customColors.Grey900,
        shape = RoundedCornerShape(16.dp)
    )
}