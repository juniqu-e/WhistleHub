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
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.whistlehub.common.data.remote.dto.response.TrackResponse.MemberInfo
import com.whistlehub.common.view.theme.CustomColors
import com.whistlehub.common.view.theme.Typography
import com.whistlehub.playlist.data.Comment
import com.whistlehub.playlist.data.CommentRepositoryImpl
import com.whistlehub.playlist.viewmodel.TrackCommentViewModel

@Preview(showBackground = true)
@Composable
fun PlayerComment(
    modifier: Modifier = Modifier, trackCommentViewModel: TrackCommentViewModel = TrackCommentViewModel(commentRepository = CommentRepositoryImpl())
) {
    val commentList = trackCommentViewModel.commentList.collectAsState(initial = emptyList())
    var newComment by remember { mutableStateOf("") }

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
                if (newComment.isNotEmpty()) {
                    newComment = ""
                }
            }){
                Icon(Icons.Rounded.Edit, contentDescription = "Send Comment", tint = CustomColors().Grey50)
            }
        }
        LazyColumn(Modifier,
            content = {
                items(commentList.value.size) { index ->
                    val comment = commentList.value[index]
                    // 댓글 항목을 표시하는 Composable 함수를 호출합니다.
                    CommentItem(comment)
                }
            }
        )
    }
}

@Composable
fun CommentItem(comment: Comment) {
    // 댓글 항목을 표시하는 UI를 구현합니다.
    // 예를 들어, Text를 사용하여 댓글 내용을 표시할 수 있습니다.
    Row(Modifier
        .fillMaxWidth()
        .padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp,
        Alignment.Start)) {
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
            .padding(horizontal = 10.dp)) {
            Row(Modifier.fillMaxWidth().height(30.dp) , verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.memberInfo.nickname,
                    color = CustomColors().Grey200,
                    style = Typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                if (comment.commentId == 2) {
                    Row {
                        IconButton({}) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit Comment", tint = CustomColors().Mint500, modifier = Modifier.size(18.dp))
                        }
                        IconButton({}) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Edit Delete", tint = CustomColors().Mint500, modifier = Modifier.size(18.dp))
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