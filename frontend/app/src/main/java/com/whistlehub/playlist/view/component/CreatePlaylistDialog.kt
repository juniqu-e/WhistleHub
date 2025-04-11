package com.whistlehub.playlist.view.component

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.whistlehub.common.util.uriToMultipartBodyPart
import com.whistlehub.common.view.signup.LabeledInputField
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import okhttp3.MultipartBody

/**
 * 플레이리스트 생성 다이얼로그 컴포넌트
 *
 * @param onDismiss 다이얼로그 닫기 이벤트 콜백
 * @param onCreatePlaylist 플레이리스트 생성 버튼 클릭 시 호출되는 콜백 (제목, 설명, 이미지)
 */
@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreatePlaylist: (String, String, MultipartBody.Part?) -> Unit
) {
    val customColors = CustomColors()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // 로컬 상태
    var playlistTitle by remember { mutableStateOf("") }
    var playlistDescription by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // 유효성 검사 상태
    var titleError by remember { mutableStateOf<String?>(null) }
    var isTitleFocused by remember { mutableStateOf(false) }
    var isDescriptionFocused by remember { mutableStateOf(false) }
    var showDeleteImageConfirm by remember { mutableStateOf(false) }

    // 이미지 선택 런처
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedImageUri = it }
    }

    val textFieldStyle = Typography.bodyMedium.copy(color = customColors.Grey50)
    val placeholderStyle = Typography.bodyMedium.copy(color = customColors.Grey300)
    val labelStyle = Typography.bodyLarge.copy(color = customColors.Grey50)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 플레이리스트 이미지 업로드 UI
        Box(
            modifier = Modifier
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            // 플레이리스트 이미지 컨테이너
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(customColors.Grey800)
                    .border(2.dp, customColors.Grey500, RoundedCornerShape(8.dp))
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "플레이리스트 이미지",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "기본 플레이리스트",
                        tint = customColors.Grey300,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }

            // 카메라 아이콘 오버레이
            Box(
                modifier = Modifier
                    .padding(start = 90.dp, top = 90.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(customColors.Grey800)
                    .border(1.dp, customColors.Grey500, CircleShape)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "사진 선택",
                    tint = customColors.Grey50,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // 이미지 삭제 버튼
        if (selectedImageUri != null) {
            Button(
                onClick = { selectedImageUri = null },
                colors = ButtonDefaults.buttonColors(
                    containerColor = customColors.Error700
                ),
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            ) {
                Text("이미지 삭제")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 플레이리스트 제목 입력 필드
        LabeledInputField(
            label = "플레이리스트 이름",
            value = playlistTitle,
            onValueChange = {
                playlistTitle = it
                titleError = if (it.isEmpty()) "플레이리스트 이름을 입력해주세요" else null
            },
            placeholder = "플레이리스트 이름을 입력하세요",
            labelStyle = labelStyle,
            textStyle = textFieldStyle,
            placeholderStyle = placeholderStyle,
            isFocused = isTitleFocused,
            onFocusChange = { isTitleFocused = it },
            errorMessage = titleError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 플레이리스트 설명 입력 필드
        LabeledInputField(
            label = "플레이리스트 설명",
            value = playlistDescription,
            onValueChange = {
                if (it.length <= 100) {
                    playlistDescription = it
                }
            },
            placeholder = "플레이리스트 설명을 입력하세요",
            labelStyle = labelStyle,
            textStyle = textFieldStyle,
            placeholderStyle = placeholderStyle,
            isFocused = isDescriptionFocused,
            onFocusChange = { isDescriptionFocused = it },
            errorMessage = null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 동작 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            // 취소 버튼
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = customColors.Grey700
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    "취소",
                    style = Typography.bodyLarge,
                    color = customColors.Grey50
                )
            }

            // 생성 버튼
            Button(
                onClick = {
                    if (playlistTitle.isNotBlank()) {
                        val image = selectedImageUri?.let {
                            uriToMultipartBodyPart(context, it)
                        }
                        onCreatePlaylist(playlistTitle, playlistDescription, image)
                    } else {
                        titleError = "플레이리스트 이름을 입력해주세요"
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = customColors.CommonButtonColor
                ),
                enabled = playlistTitle.isNotBlank(),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    "생성",
                    style = Typography.bodyLarge,
                    color = customColors.CommonTextColor
                )
            }
        }
    }
}