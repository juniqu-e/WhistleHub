package com.ssafy.backend.common.error.exception;

/**
 * <pre>InvalidAccessTokenException</pre>
 * 아이디 중복 에러
 * @author 허현준
 * @version 1.0
 * @since 2025-03-20
 */

public class InvalidAccessTokenException extends RuntimeException {
    public InvalidAccessTokenException() {
        super();
    }
}