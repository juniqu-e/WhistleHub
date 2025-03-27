package com.ssafy.backend.common.error.exception;

/**
 * <pre>InvalidOldPasswordException</pre>
 * 비밀번호 변경 시 기존 비밀번호가 일치하지 않을 때 발생하는 예외
 * @author 허현준
 * @version 1.0
 * @since 2025-03-27
 */

public class InvalidOldPasswordException extends RuntimeException {
    public InvalidOldPasswordException() {
        super();
    }
}