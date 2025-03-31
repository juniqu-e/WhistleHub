package com.whistlehub.common.data.repository

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import retrofit2.Response

/**
 * ApiRepository는 모든 API 호출 시 공통적으로 사용할 로직을 포함하는 추상 클래스입니다.
 * 이 클래스는 API 호출 결과를 처리하여 성공 및 오류 케이스에 따라 ApiResponse 객체를 반환합니다.
 */
abstract class ApiRepository {

    /**
     * API 호출을 실행하고, 성공 또는 오류에 따라 ApiResponse를 반환하는 공통 함수입니다.
     * 토큰 만료 등의 오류가 발생하면 상위 클래스에서 추가 처리를 할 수 있도록 설계되어 있습니다.
     */
    protected open suspend fun <T> executeApiCall(call: suspend () -> Response<ApiResponse<T>>): ApiResponse<T> {
        return try {
            // API 호출 실행
            val response = call()

            if (response.isSuccessful) {
                // 성공 응답: body가 null이면 기본 성공 응답 반환
                response.body() ?: ApiResponse(
                    code = "SU",
                    message = "Empty response",
                    payload = null
                )
            } else {
                // 실패 응답: errorBody를 JSON 문자열로 읽고 파싱
                val errorJson = response.errorBody()?.string() ?: ""
                val errorResponseType = object : TypeToken<ApiResponse<T>>() {}.type
                Gson().fromJson<ApiResponse<T>>(errorJson, errorResponseType)
                    ?: ApiResponse(
                        code = "ERR",
                        message = "서버로 요청이 실패했습니다: ${response.code()}",
                        payload = null
                    )
            }
        } catch (e: Exception) {
            ApiResponse(
                code = "EXC",
                message = "Exception occurred: ${e.message}",
                payload = null
            )
        }
    }
}
