package com.whistlehub.workstation.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector


data class ToastData(
    val message: String,
    val icon: ImageVector,
    val color: Color,
)

@Composable
fun rememberToastState(): MutableState<ToastData?> =
    remember { mutableStateOf(null) }