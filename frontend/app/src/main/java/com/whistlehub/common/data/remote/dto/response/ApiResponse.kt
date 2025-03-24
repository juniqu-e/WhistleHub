package com.whistlehub.common.data.remote.dto.response

/**
----------------
API 공통 응답 DTO
----------------
 **/

data class ApiResponse<T>(
    val code: String?,
    val message: String?,
    val payload: T?
)