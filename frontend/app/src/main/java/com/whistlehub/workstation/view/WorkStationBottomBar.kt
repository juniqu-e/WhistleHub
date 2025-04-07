package com.whistlehub.workstation.view

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.whistlehub.workstation.data.BottomBarActions
import com.whistlehub.workstation.di.WorkStationBottomBarProvider
import com.whistlehub.workstation.view.component.GradientIconButton
import com.whistlehub.workstation.view.component.UploadMixDialog
import com.whistlehub.workstation.view.component.UploadSheet
import javax.inject.Inject

class WorkStationBottom @Inject constructor() : WorkStationBottomBarProvider {
    @Composable
    override fun WorkStationBottomBar(
        actions: BottomBarActions,
        context: Context,
        isPlaying: Boolean
    ) {
        var menuExpanded by remember { mutableStateOf(false) }
        var showUploadDialog by remember { mutableStateOf(false) }
        var showUploadSheet by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
//                .background(Color.Transparent),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            val buttonSize = 64.dp
            val iconSize = 32.dp
            val backgroundColor = Color(0xFF1E1E1E)
            val iconColor = Color(0xFFF0F0F0)  // 약간 따뜻한 흰색
            val borderColor = Color(0xFF9575CD)  // 보라 강조

            GradientIconButton(
                icon = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                onClick = actions.onPlayedClicked,
                gradientColors = listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)) // 보라 그라디언트
            )
            GradientIconButton(
                icon = Icons.Default.Add,
                onClick = actions.onAddInstrument,
                gradientColors = listOf(Color(0xFF00F260), Color(0xFF0575E6)) // 그린-블루
            )
            GradientIconButton(
                icon = Icons.Default.CloudUpload,
                onClick = { showUploadSheet = true },
                gradientColors = listOf(Color(0xFFFF758C), Color(0xFFFF7EB3)) // 핑크 계열
            )
//            IconButton(
//                modifier = Modifier
//                    .size(buttonSize)
//                    .shadow(6.dp, shape = CircleShape)
//                    .background(backgroundColor, shape = CircleShape)
//                    .border(width = 1.dp, color = borderColor, shape = CircleShape),
//                onClick = actions.onPlayedClicked,
//                colors = IconButtonDefaults.iconButtonColors(
//                    containerColor = Color.Transparent
//                )
//            ) {
//                Icon(
//                    Icons.Default.PlayArrow,
//                    contentDescription = null,
//                    modifier = Modifier.size(iconSize),
//                    tint = iconColor
//                )
//            }
//
//            Spacer(modifier = Modifier.width(8.dp))
//
//            IconButton(
//                modifier = Modifier
//                    .size(buttonSize)
//                    .shadow(6.dp, shape = CircleShape)
//                    .background(backgroundColor, shape = CircleShape)
//                    .border(width = 1.dp, color = borderColor, shape = CircleShape),
//                onClick = actions.onPlayedClicked,
//                colors = IconButtonDefaults.iconButtonColors(
//                    containerColor = Color.Transparent
//                )
//            ) {
//                Icon(
//                    Icons.Default.Add,
//                    contentDescription = null,
//                    modifier = Modifier.size(iconSize),
//                    tint = iconColor
//                )
//            }
//
//            Spacer(modifier = Modifier.width(8.dp))
//
//            IconButton(
//                modifier = Modifier
//                    .size(buttonSize)
//                    .shadow(6.dp, shape = CircleShape)
//                    .background(backgroundColor, shape = CircleShape)
//                    .border(width = 1.dp, color = borderColor, shape = CircleShape),
//                onClick = actions.onPlayedClicked,
//                colors = IconButtonDefaults.iconButtonColors(
//                    containerColor = Color.Transparent
//                )
//            ) {
//                Icon(
//                    Icons.Default.CloudUpload,
//                    contentDescription = null,
//                    modifier = Modifier.size(iconSize),
//                    tint = iconColor
//                )
//            }
        }

        if(showUploadSheet) {
            UploadSheet(
                onDismiss = {showUploadSheet = false},
                onUploadClicked = { metadata ->
                    viewModel
                }
                ) { }
        }

        if (showUploadDialog) {
            UploadMixDialog(
                onConfirm = { name ->
                    showUploadDialog = false
                    actions.onUploadConfirm(name)
                },
                onDismiss = {
                    showUploadDialog = false
                },
                title = "너의 이름은"
            )
        }
    }
}



