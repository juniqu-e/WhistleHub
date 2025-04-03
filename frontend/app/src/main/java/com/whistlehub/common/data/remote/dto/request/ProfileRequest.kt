package com.whistlehub.common.data.remote.dto.request

import okhttp3.MultipartBody

/**
----------------------
프로필 관련 API 요청 DTO
----------------------
 **/

sealed class ProfileRequest {
    // 프로필 정보 수정
    data class UpdateProfileRequest(
        val nickname: String,
        val profileText: String
    )
    // 프로필 업로드
    data class UpdateProfileImageRequest(
        val profileImage: MultipartBody.Part
    )
    // 비밀번호 변경
    data class ChangePasswordRequest(
        val oldPassword: String,
        val newPassword: String
    )
    // 팔로우
    data class FollowRequest(
        val memberId: Int,
        val follow: Boolean
    )
}