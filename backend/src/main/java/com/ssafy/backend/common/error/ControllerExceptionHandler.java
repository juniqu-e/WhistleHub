package com.ssafy.backend.common.error;

import com.ssafy.backend.common.ApiResponse;
import com.ssafy.backend.common.error.exception.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.oxm.ValidationFailureException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/**
 * <pre>예외 전역처리</pre>
 * Exception catch of Spring Container
 *
 * @author 박병주
 * @version 1.0
 * @since 2025-03-12
 */

@RestControllerAdvice
@Slf4j
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

    @ExceptionHandler(InvalidNewPasswordException.class)
    public ApiResponse<?> invalidNewPasswordHandler(InvalidNewPasswordException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.INVALID_NEW_PASSWORD)
                .build();
    }

    @ExceptionHandler(InvalidOldPasswordException.class)
    public ApiResponse<?> invalidOldPasswordHandler(InvalidOldPasswordException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.INVALID_OLD_PASSWORD)
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

    @ExceptionHandler(TrackNotFoundException.class)
    public ApiResponse<?> trackNotFoundHandler(TrackNotFoundException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.TRACK_NOT_FOUND)
                .build();
    }
    @ExceptionHandler(NotFoundLayerException.class)
    public ApiResponse<?> layerNotFoundHandler(NotFoundLayerException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.LAYER_NOT_FOUND)
                .build();
    }

    @ExceptionHandler(EmailSendFailedException.class)
    public ApiResponse<?> emailSendFailedHandler(EmailSendFailedException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.EMAIL_SEND_FAILED)
                .build();
    }

    @ExceptionHandler(EmailNotValidatedException.class)
    public ApiResponse<?> emailNotValidatedHandler(EmailNotValidatedException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.EMAIL_NOT_VALIDATED)
                .build();
    }

    @ExceptionHandler(InvalidEmailAuthException.class)
    public ApiResponse<?> invalidEmailCodeHandler(InvalidEmailAuthException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.EMAIL_NOT_VALIDATED)
                .build();
    }

    @ExceptionHandler(NotMatchIdAndEmailException.class)
    public ApiResponse<?> notMatchIdAndEmailHandler(NotMatchIdAndEmailException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.NOT_MATCH_ID_AND_EMAIL)
                .build();
    }

    @ExceptionHandler(AlreadyValidatedEmailException.class)
    public ApiResponse<?> alreadyValidatedEmailHandler(AlreadyValidatedEmailException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.ALREADY_VALIDATED_EMAIL)
                .build();
    }

    @ExceptionHandler(NotPermittedException.class)
    public ApiResponse<?> notPermittedHandler(NotPermittedException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.NOT_PERMITTED)
                .build();
    }

    @ExceptionHandler(FileUploadFailedException.class)
    public ApiResponse<?> fileUploadFailedHandler(FileUploadFailedException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.FILE_UPLOAD_FAILED)
                .build();
    }

    @ExceptionHandler(UnreadableFileException.class)
    public ApiResponse<?> unreadableFileHandler(UnreadableFileException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.UNREADABLE_FILE)
                .build();
    }

    @ExceptionHandler(DuplicateFollowRequestException.class)
    public ApiResponse<?> duplicateFollowRequestHandler(DuplicateFollowRequestException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.DUPLICATE_FOLLOW_REQUEST)
                .build();
    }

    @ExceptionHandler(InvalidFormattedRequest.class)
    public ApiResponse<?> invalidFormattedRequestHandler(InvalidFormattedRequest e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.INVALID_FORMATTED_REQUEST)
                .build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<?> httpMessageNotReadableHandler(HttpMessageNotReadableException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.INVALID_FORMATTED_REQUEST)
                .build();
    }
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ApiResponse<?> badRequestHandler(HandlerMethodValidationException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.INVALID_FORMATTED_REQUEST)
                .build();
    }

    @ExceptionHandler(S3FileException.class)
    public ApiResponse<?> s3FileExceptionHandler(S3FileException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.S3_ERROR)
                .build();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiResponse<?> missingServletRequestParameterHandler(MissingServletRequestParameterException e) {
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.PARAMETER_REQUIRED)
                .build();
    }

    // 이외의 정의되지 않은 서버 에러처리
    @ExceptionHandler(Exception.class)
    public ApiResponse<?> serverErrorHandler(Exception e) {
        log.error(e.getMessage(), e);
        return new ApiResponse.builder<Object>()
                .errorStatus(ResponseType.SERVER_ERROR)
                .build();
    }

}
