package com.whistlehub.workstation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.workstation.data.BottomBarActions
import com.whistlehub.workstation.di.WorkStationBottomBarProvider
import javax.inject.Inject

class WorkStationBottom @Inject constructor() : WorkStationBottomBarProvider {
    @Composable
    override fun WorkStationBottomBar(actions: BottomBarActions) {
        var menuExpanded by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .background(Color.Transparent),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Button(
                modifier = Modifier.weight(1f), onClick = actions.onPlayedClicked,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("재생", color = Color.Black, style = Typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = actions.onTrackUploadClicked,
//                onClick = { menuExpanded = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Menu, contentDescription = null)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("업로드", color = Color.Black, style = Typography.titleMedium)
                }
            }
        }

//        DropdownMenu(
//            expanded = menuExpanded,
//            onDismissRequest = { menuExpanded = false },
//            offset = DpOffset(x = (-40).dp, y = (-28).dp),
//            containerColor = Color.LightGray
//        ) {
//            DropdownMenuItem(
//                text = { Text("Save") },
//                onClick = {
//                    menuExpanded = false
//                },
//                leadingIcon = {
//                    Icon(
//                        Icons.Outlined.Save,
//                        contentDescription = "Save Track"
//                    )
//                }
//            )
//            DropdownMenuItem(
//                text = { Text("Upload") },
//                onClick = {
//                    menuExpanded = false
//                },
//                leadingIcon = {
//                    Icon(
//                        Icons.Outlined.Upload,
//                        contentDescription = "Save Track"
//                    )
//                }
//            )
//        }
    }
}



