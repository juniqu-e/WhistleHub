package com.ssafy.backend.common.error.exception;

/**
 * <pre>NotFoundPageException</pre>
 * 없는 정보 또는 페이지에 대한 에러
 * @author 박병주
 * @version 1.0
 * @since 2025-03-12
 */

public class NotFoundPlaylistException extends RuntimeException {
    public NotFoundPlaylistException() {
        super();
    }
}