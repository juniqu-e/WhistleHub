package com.whistlehub.workstation.data

data class BottomBarActions(
    val onPlayedClicked: () -> Unit,
    val onTrackSavedClicked: () -> Unit,
    val onTrackUploadClicked: () -> Unit,
    val onTrackDownloadClicked: () -> Unit,
    val onExitClicked: () -> Unit,
)

