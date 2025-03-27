package com.whistlehub.workstation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.whistlehub.workstation.data.BottomBarActions
import com.whistlehub.workstation.di.WorkStationBottomBarProvider
import javax.inject.Inject

class WorkStationBottom @Inject constructor() : WorkStationBottomBarProvider {
    @Composable
    override fun WorkStationBottomBar(actions: BottomBarActions) {
        var menuExpanded by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp)
                .background(CustomColors().Grey700)
        ) {
            Row(modifier = Modifier.align(Alignment.CenterStart)) {
                IconButton(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(horizontal = 4.dp),
                    onClick = actions.onPlayedClicked
                ) {
                    Icon(
                        modifier = Modifier.fillMaxSize(),
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Track",
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(horizontal = 4.dp),
                    onClick = { }
                ) {
                    Icon(
                        modifier = Modifier.fillMaxSize(),
                        imageVector = Icons.Default.Circle,
                        contentDescription = "Record",
                        tint = Color.Red
                    )
                }
            }

            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(horizontal = 16.dp)
                    .size(40.dp)
            ) {
                Icon(
                    modifier = Modifier.fillMaxSize(),
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Bottom Menu"
                )
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    DropdownMenuItem(
                        text = { Text("Save") },
                        onClick = {
                            menuExpanded = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Save,
                                contentDescription = "Save Track"
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Upload") },
                        onClick = {
                            menuExpanded = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Upload,
                                contentDescription = "Save Track"
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Download") },
                        onClick = {
                            menuExpanded = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Download,
                                contentDescription = "Save Track"
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Exit") },
                        onClick = {
                            actions.onExitClicked()
                            menuExpanded = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.AutoMirrored.Outlined.Logout,
                                contentDescription = "Save Track"
                            )
                        }
                    )
                }
            }
        }
    }
}

