package com.whistlehub.workstation.data

import android.content.Context

data class BottomBarActions(
    val onPlayedClicked: () -> Unit,
    val onTrackUploadClicked: () -> Unit,
    val onAddInstrument: () -> Unit,
    val onUploadConfirm: (String) -> Unit = {},
)

