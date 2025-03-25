package com.ssafy.backend.common.error.exception;

/**
 * <pre>ExpiredAccessTokenException</pre>
 * Access token 만료 에러
 *
 * @author 허현준
 * @version 1.0
 * @since 2025-03-25
 */

public class ExpiredAccessTokenException extends RuntimeException {
    public ExpiredAccessTokenException() {
        super();
    }
}