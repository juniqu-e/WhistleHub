package com.whistlehub.playlist.view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.whistlehub.common.view.theme.CustomColors

/**
 * 플레이리스트 삭제 확인 다이얼로그
 *
 * @param onDismiss 취소 버튼 클릭 시 호출되는 콜백
 * @param onConfirm 확인 버튼 클릭 시 호출되는 콜백
 */
@Composable
fun DeletePlaylistDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val colors = CustomColors()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("플레이리스트 삭제") },
        text = { Text("플레이리스트를 삭제하시겠습니까?") },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.Error700,
                    contentColor = colors.CommonTextColor
                )
            ) {
                Text("삭제")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = colors.CommonTextColor
                ),
                border = BorderStroke(1.dp, colors.CommonOutLineColor)
            ) {
                Text("취소")
            }
        }
    )
}

/**
 * 플레이리스트 재생 확인 다이얼로그
 *
 * @param onDismiss 취소 버튼 클릭 시 호출되는 콜백
 * @param onConfirm 확인 버튼 클릭 시 호출되는 콜백
 */
@Composable
fun PlayPlaylistDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val colors = CustomColors()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("플레이리스트 재생") },
        text = { Text("플레이리스트를 재생하시겠습니까?") },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.CommonButtonColor,
                    contentColor = colors.CommonTextColor
                )
            ) {
                Text("재생")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = colors.CommonTextColor
                ),
                border = BorderStroke(1.dp, colors.CommonOutLineColor)
            ) {
                Text("취소")
            }
        }
    )
}