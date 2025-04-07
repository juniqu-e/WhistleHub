package com.whistlehub.workstation.di

import android.content.Context
import androidx.compose.runtime.Composable
import com.whistlehub.workstation.data.BottomBarActions

interface WorkStationBottomBarProvider {
    @Composable
    fun WorkStationBottomBar(
        actions: BottomBarActions,
        context: Context,
        isPlaying: Boolean,
        showBottomSheet: Boolean
    )
}