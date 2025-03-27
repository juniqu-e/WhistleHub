package com.ssafy.backend.common.error.exception;

/**
 * <pre>NotFoundTrackException</pre>
 * 없는 정보 또는 레이어에 대한 에러
 * @author 박병주
 * @version 1.0
 * @since 2025-03-26
 */

public class NotFoundLayerException extends RuntimeException {
    public NotFoundLayerException() {
        super();
    }
}