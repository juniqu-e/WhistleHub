package com.ssafy.backend.common.error.exception;

/**
 * <pre>NotMatchIdAndEmailException</pre>
 * 비밀번호 찾기 도중, 아이디와 이메일이 일치하지 않을 때 발생하는 예외
 *
 * @author 허현준
 * @version 1.0
 * @since 2025-03-26
 */

public class NotMatchIdAndEmailException extends RuntimeException {
    public NotMatchIdAndEmailException() {
        super();
    }
}