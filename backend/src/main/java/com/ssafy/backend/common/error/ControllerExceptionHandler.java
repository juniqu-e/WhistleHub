package com.ssafy.backend.common.error;

import com.ssafy.backend.common.ApiResponse;
import com.ssafy.backend.common.error.exception.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <pre>예외 전역처리</pre>
 * Exception catch of Spring Container
 *
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

    @ExceptionHandler(NotFoundMemberException.class)
    public ApiResponse<?> notFoundMemberHandler(NotFoundMemberException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.NOT_FOUND_MEMBER)
                .build();
    }

    @ExceptionHandler(NotFoundPlaylistException.class)
    public ApiResponse<?> illegalArgumentHandler(NotFoundPlaylistException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.PLAYLIST_NOT_FOUND)
                .build();
    }

    @ExceptionHandler(DuplicateIdException.class)
    public ApiResponse<?> duplicateIdHandler(DuplicateIdException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.DUPLICATE_ID)
                .build();
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ApiResponse<?> duplicateEmailHandler(DuplicateEmailException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.DUPLICATE_EMAIL)
                .build();
    }

    @ExceptionHandler(DuplicateNicknameException.class)
    public ApiResponse<?> duplicateNicknameHandler(DuplicateNicknameException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.DUPLICATE_NICKNAME)
                .build();
    }

    @ExceptionHandler(MissingParameterException.class)
    public ApiResponse<?> badRequestHandler(MissingParameterException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.PARAMETER_REQUIRED)
                .build();
    }

    @ExceptionHandler(NotFoundException.class)
    public ApiResponse<?> notFoundHandler(NotFoundException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.NOT_FOUND)
                .build();
    }

    @ExceptionHandler(DuplicateTrackException.class)
    public ApiResponse<?> duplicateTrackHandler(DuplicateTrackException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.DUPLICATE_TRACK)
                .build();
    }

    @ExceptionHandler(InvalidAccessTokenException.class)
    public ApiResponse<?> invalidAccessTokenHandler(InvalidAccessTokenException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.INVALID_ACCESS_TOKEN)
                .build();
    }

    @ExceptionHandler(ExpiredAccessTokenException.class)
    public ApiResponse<?> expiredAccessTokenHandler(ExpiredAccessTokenException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.EXPIRED_ACCESS_TOKEN)
                .build();
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ApiResponse<?> invalidRefreshTokenHandler(InvalidRefreshTokenException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.INVALID_REFRESH_TOKEN)
                .build();
    }

    @ExceptionHandler(ExpiredRefreshTokenException.class)
    public ApiResponse<?> expiredRefreshTokenHandler(ExpiredRefreshTokenException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.EXPIRED_REFRESH_TOKEN)
                .build();
    }

    @ExceptionHandler(EmailSendFailedException.class)
    public ApiResponse<?> emailSendFailedHandler(EmailSendFailedException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.EMAIL_SEND_FAILED)
                .build();
    }

    @ExceptionHandler(InvalidEmailAuthException.class)
    public ApiResponse<?> invalidEmailCodeHandler(InvalidEmailAuthException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.INVALID_EMAIL_AUTH)
                .build();
    }

    @ExceptionHandler(NotMatchIdAndEmailException.class)
    public ApiResponse<?> notMatchIdAndEmailHandler(NotMatchIdAndEmailException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.NOT_MATCH_ID_AND_EMAIL)
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
