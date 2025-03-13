package com.ssafy.backend.common.error;

import com.ssafy.backend.common.ApiResponse;
import com.ssafy.backend.common.error.exception.NotFoundPageException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <pre>예외 전역처리</pre>
 * Exception catch of Spring Container
 * @author 박병주
 * @version 1.0
 * @since 2025-03-12
 */

@RestControllerAdvice
public class ControllerExceptionHandler {


    @ExceptionHandler(NotFoundPageException.class)
    public ApiResponse<?> notFoundPageHandler(NotFoundPageException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.NOT_FOUND_PAGE)
                .build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ApiResponse<?> entityNotFoundHandler(EntityNotFoundException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.SERVER_ERROR)
                .build();
    }

    // 이외의 정의되지 않은 서버 에러처리
    @ExceptionHandler(Exception.class)
    public ApiResponse<?> serverErrorHandler(Exception e) {
        e.printStackTrace();
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.SERVER_ERROR)
                .build();
    }

}
