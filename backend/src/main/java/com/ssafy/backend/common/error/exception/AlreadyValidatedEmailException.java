package com.ssafy.backend.common.error.exception;

/**
 * <pre>AlreadyValidatedEmailException</pre>
 * 이미 인증된 이메일입니다.
 * @author 허현준
 * @version 1.0
 * @since 2025-03-26
 */

public class AlreadyValidatedEmailException extends RuntimeException {
    public AlreadyValidatedEmailException() {
        super();
    }
}