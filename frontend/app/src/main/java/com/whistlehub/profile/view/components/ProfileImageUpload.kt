package com.whistlehub.profile.view.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun ProfileImageUpload(
    profileImageUrl: String,
    onImageSelected: (Uri?) -> Unit,
    onDeleteImage: () -> Unit
) {
    var localImageUri by remember { mutableStateOf<Uri?>(null) }

    // 갤러리 접근 런처
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            localImageUri = it
            onImageSelected(it)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 이미지 표시
        AsyncImage(
            model = localImageUri ?: profileImageUrl,
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // 이미지 수정 / 삭제 버튼
        Column {
            Button(onClick = { launcher.launch("image/*") }) {
                Text("이미지 수정")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(onClick = {
                localImageUri = null
                onDeleteImage()
            }) {
                Text("이미지 삭제")
            }
        }
    }
}

