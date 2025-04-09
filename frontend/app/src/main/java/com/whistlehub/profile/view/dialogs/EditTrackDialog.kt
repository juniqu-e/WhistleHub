package com.whistlehub.profile.view.dialogs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.whistlehub.common.data.remote.dto.response.TrackResponse
import com.whistlehub.common.util.uriToMultipartBodyPart
import com.whistlehub.common.view.signup.LabeledInputField
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import okhttp3.MultipartBody

@Composable
fun EditTrackDialog(
    track: TrackResponse.GetTrackDetailResponse,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, visibility: Boolean, image: MultipartBody.Part?) -> Unit
) {
    val customColors = CustomColors()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var editedTitle by remember { mutableStateOf(track.title) }
    var editedDescription by remember { mutableStateOf(track.description ?: "") }
    var editedVisibility by remember { mutableStateOf(true) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var titleError by remember { mutableStateOf<String?>(null) }
    var isTitleFocused by remember { mutableStateOf(false) }
    var isDescriptionFocused by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selectedImageUri = it }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = customColors.Grey900
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "트랙 수정하기",
                    style = Typography.titleLarge,
                    color = customColors.Grey50
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 이미지 업로드
                Box(
                    modifier = Modifier.padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
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
                                contentDescription = "트랙 이미지",
                                modifier = Modifier.fillMaxWidth(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (track.imageUrl != null) {  // 추가
                            AsyncImage(
                                model = track.imageUrl,
                                contentDescription = "기존 트랙 이미지",
                                modifier = Modifier.fillMaxWidth(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "기본 이미지",
                                tint = customColors.Grey300,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }

                    // 카메라 아이콘
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

                // 제목 입력
                LabeledInputField(
                    label = "트랙 제목",
                    value = editedTitle,
                    onValueChange = {
                        editedTitle = it
                        titleError = if (it.isEmpty()) "트랙 제목을 입력해주세요" else null
                    },
                    placeholder = "트랙 제목을 입력하세요",
                    labelStyle = Typography.bodyLarge.copy(color = customColors.Grey50),
                    textStyle = Typography.bodyMedium.copy(color = customColors.Grey50),
                    placeholderStyle = Typography.bodyMedium.copy(color = customColors.Grey300),
                    isFocused = isTitleFocused,
                    onFocusChange = { isTitleFocused = it },
                    errorMessage = titleError,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 설명 입력
                LabeledInputField(
                    label = "트랙 설명",
                    value = editedDescription,
                    onValueChange = {
                        if (it.length <= 200) {
                            editedDescription = it
                        }
                    },
                    placeholder = "트랙 설명을 입력하세요",
                    labelStyle = Typography.bodyLarge.copy(color = customColors.Grey50),
                    textStyle = Typography.bodyMedium.copy(color = customColors.Grey50),
                    placeholderStyle = Typography.bodyMedium.copy(color = customColors.Grey300),
                    isFocused = isDescriptionFocused,
                    onFocusChange = { isDescriptionFocused = it },
                    errorMessage = null,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 공개 여부 스위치
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "공개 여부",
                        style = Typography.bodyLarge,
                        color = customColors.Grey200
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = editedVisibility,
                        onCheckedChange = { editedVisibility = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = customColors.CommonFocusColor,
                            uncheckedThumbColor = customColors.Grey700
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 하단 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
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

                    Button(
                        onClick = {
                            val image = selectedImageUri?.let { uri ->
                                uriToMultipartBodyPart(context, uri)
                            }
                            onSave(editedTitle, editedDescription, editedVisibility, image)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = customColors.CommonButtonColor
                        ),
                        enabled = editedTitle.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = customColors.CommonTextColor,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "저장",
                                style = Typography.bodyLarge,
                                color = customColors.CommonTextColor
                            )
                        }
                    }
                }
            }
        }
    }
}
