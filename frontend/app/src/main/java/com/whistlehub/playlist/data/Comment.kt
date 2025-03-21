package com.whistlehub.playlist.data

import com.whistlehub.common.data.remote.dto.response.TrackResponse.MemberInfo

data class Comment(
    val commentId: Int,
    val memberInfo: MemberInfo,
    val comment: String
)
