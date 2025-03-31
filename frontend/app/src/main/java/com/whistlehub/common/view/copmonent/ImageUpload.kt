package com.whistlehub.common.view.copmonent

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.whistlehub.R
import com.whistlehub.common.view.theme.CustomColors

@Composable
fun ImageUpload(
    onChangeImage: (Uri) -> Unit // 선택된 이미지 URI를 업로드하는 함수
) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            onChangeImage(it) // 선택된 이미지 URI를 업로드하는 함수 호출
        } ?: run {
            imageUri = null
            onChangeImage(Uri.EMPTY) // 이미지가 선택되지 않았을 때 빈 URI 전달
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = if (imageUri != null) {
                rememberAsyncImagePainter(imageUri) // 선택한 이미지
            } else {
                painterResource(id = R.drawable.default_track) // 기본 이미지
            },
            contentDescription = "Selected Image",
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(2.dp, Color.Gray, RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Row(
            Modifier.padding(top = 5.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(onClick = { launcher.launch("image/*") }) {
                Text("이미지 선택")
            }
            Button(
                onClick = {
                    imageUri = null
                    onChangeImage(Uri.EMPTY) // 이미지 삭제 시 빈 URI 전달
                },
                enabled = imageUri != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CustomColors().Error500, contentColor = CustomColors().Grey50
                ),
            ) {
                Text("이미지 삭제")
            }
        }
    }
}
