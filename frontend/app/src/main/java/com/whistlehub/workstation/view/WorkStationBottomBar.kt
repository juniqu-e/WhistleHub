package com.whistlehub.workstation.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.whistlehub.workstation.di.WorkStationBottomBarProvider
import javax.inject.Inject

class WorkStationBottom @Inject constructor() : WorkStationBottomBarProvider {
    @Composable
    override fun WorkStationBottomBar() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Custom Bottom Bar",
                color = Color.White
            )
        }
    }
}