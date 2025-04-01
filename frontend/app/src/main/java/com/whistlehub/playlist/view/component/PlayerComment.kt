package com.whistlehub.playlist.view.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.whistlehub.common.data.remote.dto.response.TrackResponse
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.playlist.viewmodel.TrackPlayViewModel
import kotlinx.coroutines.launch

@Preview(showBackground = true)
@Composable
fun PlayerComment(
    modifier: Modifier = Modifier,
    trackPlayViewModel: TrackPlayViewModel = hiltViewModel(),
) {
    val coroutineScope = rememberCoroutineScope()
    val commentList by trackPlayViewModel.commentList.collectAsState(initial = emptyList())
    val user by trackPlayViewModel.user.collectAsState(initial = null)
    val currentTrack by trackPlayViewModel.currentTrack.collectAsState(initial = null)

    var newComment by remember { mutableStateOf("") }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var commentId by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        trackPlayViewModel.getTrackComment(currentTrack?.trackId.toString())
    }

    Column(modifier = modifier.background(CustomColors().Grey950.copy(alpha = 0.7f))) {
        Row(modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .border(1.dp, CustomColors().Grey50, RoundedCornerShape(5.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween)
        {
            TextField(
                value = newComment,
                onValueChange = { newComment = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "댓글을 입력하세요",
                        color = CustomColors().Grey50,
                        style = Typography.bodyMedium,
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                ),
                shape = RoundedCornerShape(10.dp),
            )
            IconButton({
                if (newComment.isNotEmpty() && currentTrack != null) {
                    coroutineScope.launch {
                        trackPlayViewModel.createTrackComment(currentTrack!!.trackId, newComment)
                        newComment = ""
                    }
                }
            }){
                Icon(Icons.Rounded.Edit, contentDescription = "Send Comment", tint = CustomColors().Grey50)
            }
        }

        // 댓글 목록을 표시하는 LazyColumn을 사용합니다.
        if (commentList?.isEmpty() == true) {
            Text(
                text = "댓글이 없습니다.",
                color = CustomColors().Grey50,
                style = Typography.bodyLarge,
                modifier = Modifier.padding(10.dp)
            )
        } else {
            LazyColumn(Modifier.fillMaxWidth()) {
                if (commentList != null && commentList!!.isNotEmpty()) {
                    // 댓글 목록을 반복하여 각 댓글 항목을 표시합니다.
                    items(commentList!!.size) { index ->
                        val comment = commentList!![index]
                        // 댓글 항목을 표시하는 Composable 함수를 호출합니다.
                        CommentItem(comment, userId = user?.memberId,
                            onDelete = { id ->
                                commentId = id
                                showDeleteDialog = true
                            },
                            onUpdate = { id, comment ->
                                coroutineScope.launch {
                                    trackPlayViewModel.updateTrackComment(id, comment)
                                }
                            }
                        )
                    }
                } else {
                    item {
                        Text(
                            text = "댓글이 없습니다.",
                            color = CustomColors().Grey50,
                            style = Typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("댓글 삭제") },
            text = { Text("정말로 댓글을 삭제하시겠습니까?") },
            confirmButton = {
                Button(onClick = {
                    coroutineScope.launch {
                        trackPlayViewModel.deleteTrackComment(commentId)
                        showDeleteDialog = false
                    } },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CustomColors().Error700,
                        contentColor = CustomColors().Grey950
                    )
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CustomColors().Grey400,
                        contentColor = CustomColors().Grey950
                    )
                ) {
                    Text("취소")
                }
            }
        )
    }
}

@Composable
fun CommentItem(comment: TrackResponse.GetTrackComment,
                userId: Int? = null,
                onUpdate: (Int, String) -> Unit = { _, _ -> },
                onDelete: (Int) -> Unit = {},
                ) {
    // 댓글 항목을 표시하는 UI를 구현합니다.

    var isEditing by remember { mutableStateOf(false) }
    var updatedComment by remember { mutableStateOf(comment.comment) }

    Row(Modifier
        .fillMaxWidth()
        .height(100.dp)
        .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start)
    ) {
        AsyncImage(
            model = comment.memberInfo.profileImage,
            contentDescription = comment.memberInfo.nickname,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Column(Modifier
            .weight(1f)
            .padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically)
        ) {
            // 수정 중
            if (isEditing) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CustomColors().Grey50, RoundedCornerShape(5.dp)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp))
                {
                    TextField(
                        value = updatedComment,
                        onValueChange = { updatedComment = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                text = "댓글을 입력하세요",
                                color = CustomColors().Grey50,
                                style = Typography.bodyMedium,
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                        ),
                        shape = RoundedCornerShape(10.dp),
                    )
                    IconButton({
                        if (updatedComment.isNotEmpty()) {
                            onUpdate(comment.commentId, updatedComment)
                            isEditing = false
                        }
                    }) {
                        Icon(Icons.Rounded.Check,
                            contentDescription = "Send Comment",
                            tint = CustomColors().Grey50
                        )
                    }
                    IconButton({
                        isEditing = false
                        updatedComment = comment.comment
                    }) {
                        Icon(Icons.Rounded.Close,
                            contentDescription = "Close Edit",
                            tint = CustomColors().Grey50
                        )
                    }
                }
            }
            // 수정 중이 아닐 때
            else {
                // 닉네임 - 수정, 삭제 버튼
                Row(Modifier
                    .fillMaxWidth()
                    .height(30.dp) , verticalAlignment = Alignment.CenterVertically)
                {
                    Text(
                        text = comment.memberInfo.nickname,
                        color = CustomColors().Grey200,
                        style = Typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    if (comment.memberInfo.memberId == userId && !isEditing) {
                        Row {
                            IconButton({
                                isEditing = true
                            }) {
                                Icon(Icons.Rounded.Edit,
                                    contentDescription = "Edit Comment",
                                    tint = CustomColors().Mint500,
                                    modifier = Modifier.size(18.dp))
                            }
                            IconButton({
                                onDelete(comment.commentId)
                            }) {
                                Icon(Icons.Rounded.Delete,
                                    contentDescription = "Edit Delete",
                                    tint = CustomColors().Mint500,
                                    modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
                Text(
                    text = comment.comment,
                    color = CustomColors().Grey50,
                    style = Typography.bodyLarge
                )
            }
        }
    }
}