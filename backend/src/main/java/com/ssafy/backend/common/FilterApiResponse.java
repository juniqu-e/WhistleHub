package com.ssafy.backend.common;

import com.ssafy.backend.common.error.ResponseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <pre>filter에서의 API 공통 반환 객체</pre>
 *
 * @author 허현준
 * @version 1.0
 * @since 2025-03-25
 */

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterApiResponse<T> {
    private String message;
    private String code;
    private T payload;

    // ResponseType을 통한 응답 설정
    // builder().~~~~~~.build().setResponseType(ResponseType.~~~~~~)
    public FilterApiResponse<T> setResponseType(ResponseType responseType) {
        this.message = responseType.getMessage();
        this.code = responseType.getCode();
        return this;
    }
}
