package com.whistlehub.workstation.data

data class BottomBarActions(
    val onPlayedClicked: () -> Unit,
    val onTrackUploadClicked: () -> Unit,
    val onAddInstrument: () -> Unit,
    val onUploadConfirm: (String) -> Unit = {},
    val onUploadTrackConfirm: (UploadMetadata) -> Unit = {},
)

