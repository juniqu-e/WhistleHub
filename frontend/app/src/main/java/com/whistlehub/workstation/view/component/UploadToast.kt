package com.whistlehub.workstation.view.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.whistlehub.workstation.data.ToastData
import kotlinx.coroutines.delay

@Composable
fun CustomToast(
    toastData: ToastData?,
    onDismiss: () -> Unit,
    position: Alignment = Alignment.TopCenter
) {
    if (toastData != null) {
        LaunchedEffect(toastData) {
            delay(1500)
            onDismiss()
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = toastData.color),
                modifier = Modifier
                    .padding(16.dp)
                    .wrapContentSize()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(toastData.icon, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(toastData.message, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
