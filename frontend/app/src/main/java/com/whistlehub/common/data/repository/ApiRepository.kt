package com.whistlehub.common.data.repository

import com.whistlehub.common.data.remote.dto.response.ApiResponse
import retrofit2.Response
import retrofit2.http.HTTP

/**
 * ApiRepository는 모든 API 호출 시 공통적으로 사용할 로직을 포함하는 추상 클래스입니다.
 * 이 클래스는 API 호출 결과를 처리하여 성공 및 오류 케이스에 따라 ApiResponse 객체를 반환합니다.
 **/
abstract class ApiRepository {

    /**
     * API 호출을 실행하고, 성공 또는 오류에 따라 ApiResponse를 반환하는 공통 함수입니다.
     *
     * @param call suspend 람다 함수로, Retrofit의 Response<ApiResponse<T>>를 반환하는 API 호출을 실행합니다.
     * @return API 호출의 결과를 나타내는 ApiResponse<T>. 호출 성공 시 payload를 포함하며, 실패 또는 예외 발생 시 오류 정보를 포함합니다.
     */
    protected open suspend fun <T> executeApiCall(call: suspend () -> Response<ApiResponse<T>>): ApiResponse<T> {
        try {
            // API 호출을 실행하고 응답을 받습니다.
            val response = call()

            // 응답이 성공적(HTTP 2xx)인 경우
            if (response.isSuccessful) {
                // 응답 본문이 null이 아니라면 반환합니다.
                // 본문이 null인 경우, 성공 코드("SU")와 함께 "Empty response" 메시지를 가진 ApiResponse를 반환합니다.
                return response.body() ?: ApiResponse(
                    code = "SU",
                    message = "Empty response",
                    payload = null
                )
            } else {
                // 응답이 실패(HTTP 2xx가 아닌 경우)하면, 실패 코드("ERR")와 함께 해당 HTTP 상태 코드를 메시지에 포함시켜 ApiResponse를 반환합니다.
                return ApiResponse(
                    code = "ERR",
                    message = "API call failed with code: ${response.code()}",
                    payload = null
                )
            }
        } catch (e: Exception) {
            // API 호출 도중 예외가 발생하면, 예외 코드("EXC")와 함께 예외 메시지를 포함한 ApiResponse를 반환합니다.
            return ApiResponse(
                code = "EXC",
                message = "Exception occurred: ${e.message}",
                payload = null
            )
        }
    }
}
