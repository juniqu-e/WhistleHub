package com.whistlehub.playlist.data

import com.whistlehub.common.data.remote.dto.response.TrackResponse.MemberInfo

interface CommentRepository {
    fun getComments(trackId: String): List<Comment>
    fun addComment(trackId: String, comment: Comment): Boolean
    fun deleteComment(trackId: String, commentId: Int): Boolean
}

class CommentRepositoryImpl : CommentRepository {
    private val comments = mutableMapOf<String, MutableList<Comment>>()

    override fun getComments(trackId: String): List<Comment> {
//        return comments[trackId] ?: emptyList()
        // 임시 하드 코딩
        return listOf(
            Comment(1, MemberInfo(1, "User1", "https://picsum.photos/200/300?random=5"), "This is a comment."),
            Comment(2, MemberInfo(2, "User2", "https://picsum.photos/200/300?random=6"), "This is another comment."),
            Comment(3, MemberInfo(3, "User3", "https://picsum.photos/200/300?random=7"), "This is yet another comment.")
        )
    }

    override fun addComment(trackId: String, comment: Comment): Boolean {
        if (comments[trackId] == null) {
            comments[trackId] = mutableListOf()
        }
        return comments[trackId]?.add(comment) ?: false
    }

    override fun deleteComment(trackId: String, commentId: Int): Boolean {
        return comments[trackId]?.removeIf { it.commentId == commentId } ?: false
    }
}