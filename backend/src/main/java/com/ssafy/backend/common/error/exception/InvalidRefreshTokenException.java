package com.ssafy.backend.common.error.exception;

/**
 * <pre>InvalidRefreshTokenException</pre>
 * Refresh token 유효하지 않은 에러
 *
 * @author 허현준
 * @version 1.0
 * @since 2025-03-20
 */

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException() {
        super();
    }
}