package com.ssafy.backend.common.error.exception;

/**
 * <pre>InvalidFormattedRequest</pre>
 *
 * 잘못된 형식의 요청을 나타내는 예외 클래스입니다.
 * @author 허현준
 * @version 1.0
 * @since 2025-03-28
 */

public class InvalidFormattedRequest extends RuntimeException {
    public InvalidFormattedRequest() {
        super();
    }
}