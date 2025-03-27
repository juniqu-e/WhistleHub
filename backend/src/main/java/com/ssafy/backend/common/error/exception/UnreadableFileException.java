package com.ssafy.backend.common.error.exception;

/**
 * <pre>FileUploadFailedException</pre>
 * 이미지를 읽을 수 없을때 발생하는 에러
 * @author 허현준
 * @version 1.0
 * @since 2025-03-27
 */

public class UnreadableFileException extends RuntimeException {
    public UnreadableFileException() {
        super();
    }
}