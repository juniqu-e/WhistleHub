package com.ssafy.backend.common.error.exception;

/**
 * <pre>EmailSendFailedException</pre>
 * 이메일 발송 실패 예외
 * @author 허현준
 * @version 1.0
 * @since 2025-03-25
 */

public class EmailSendFailedException extends RuntimeException {
    public EmailSendFailedException() {
        super();
    }
}