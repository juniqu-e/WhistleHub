package com.whistlehub.common.data.remote.dto.response

data class ApiResponse<T>(
    val code: String,
    val message: String,
    val payload: T?
)