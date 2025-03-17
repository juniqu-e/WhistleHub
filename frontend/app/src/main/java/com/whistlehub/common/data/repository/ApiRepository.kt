package com.whistlehub.common.data.repository

import com.whistlehub.common.data.remote.dto.response.ApiResponse
import retrofit2.Response
import retrofit2.http.HTTP

abstract class ApiRepository {
    protected open suspend fun <T> executeApiCall(call: suspend () -> Response<ApiResponse<T>>): ApiResponse<T> {
        try {
            val response = call()
            if (response.isSuccessful) {
                return response.body() ?: ApiResponse(
                    code = "SU",
                    message = "Empty response body",
                    payload = null
                )
            } else {
                // Handle error response
                return ApiResponse(
                    code = "ERR",
                    message = "API call failed with code: ${response.code()}",
                    payload = null
                )
            }
        } catch (e: Exception) {
            return ApiResponse(
                code = "EXC",
                message = "Exception occurred: ${e.message}",
                payload = null
            )
        }
    }
}