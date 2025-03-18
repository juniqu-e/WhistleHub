package com.whistlehub.common.data.remote.dto.request

import okhttp3.MultipartBody

sealed class ProfileRequest {
    data class UpdateProfileRequest(
        val nickname: String,
        val profileImage: String,
        val profileText: String
    )

    data class UploadProfileImageRequest(
        val image: MultipartBody.Part
    )

    data class ChangePasswordRequest(
        val oldPassword: String,
        val newPassword: String
    )

    data class FollowRequest(
        val memberId: Int,
        val follow: Boolean
    )
}