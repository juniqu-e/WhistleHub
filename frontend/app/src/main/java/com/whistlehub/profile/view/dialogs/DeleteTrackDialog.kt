package com.whistlehub.profile.view.dialogs

import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.whistlehub.common.view.theme.CustomColors

/**
 * Dialog for confirming track deletion
 *
 * @param isLoading Whether the delete operation is in progress
 * @param onDismiss Called when user dismisses the dialog
 * @param onConfirm Called when user confirms deletion
 */
@Composable
fun DeleteTrackDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val customColors = CustomColors()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("트랙 삭제", color = customColors.Error700) },
        text = {
            Text(
                "이 트랙을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.",
                color = customColors.Grey200
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = customColors.Error700,
                    contentColor = customColors.Grey50
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = customColors.Grey50,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("삭제")
                }
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
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    )
}