package com.whistlehub.common.data.remote.dto.request

sealed class ProfileRequest {
    data class UpdateProfile(
        val nickname: String,
        val profileImage: String,
        val profileText: String
    )

    data class ChangePassword(
        val oldPassword: String,
        val newPassword: String
    )

    data class Follow(
        val memberId: String
    )
}