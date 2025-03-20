package com.whistlehub.playlist.viewmodel

import androidx.lifecycle.ViewModel
import com.whistlehub.playlist.data.Comment
import com.whistlehub.playlist.data.CommentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class TrackCommentViewModel @Inject constructor(private val commentRepository: CommentRepository) : ViewModel() {
    private val _commentList = MutableStateFlow<List<Comment>>(emptyList())
    val commentList: StateFlow<List<Comment>> get() = _commentList

    init {
        loadComments("1")
    }

    fun loadComments(trackId: String) {
        _commentList.value = commentRepository.getComments(trackId)
    }

    fun addComment(comment: Comment) {
        _commentList.value = _commentList.value + comment
    }

    fun removeComment(commentId: Int) {
        _commentList.value = _commentList.value.filter { it.commentId != commentId }
    }

    fun updateComment(commentId: Int, newComment: String) {
        _commentList.value = _commentList.value.map {
            if (it.commentId == commentId) it.copy(comment = newComment) else it
        }
    }
}