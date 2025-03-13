package com.ssafy.whistlehub.common.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * <pre>Http Status 관련 상수</pre>
 *
 * @author 박병주
 * @version 1.0
 * @since 2025-03-12
 */

@Getter
public enum ResponseType {
    // HTTP Status 200
    SUCCESS("SU", HttpStatus.OK, "요청이 성공적으로 처리되었습니다."),

    // HTTP Status 400
//    VALIDATION_FAILED("VF", HttpStatus.BAD_REQUEST, "입력값 검증에 실패하였습니다."),
//    DUPLICATE_EMAIL("DE", HttpStatus.BAD_REQUEST, "중복된 이메일입니다."),
//    DUPLICATE_NICKNAME("DN", HttpStatus.BAD_REQUEST, "중복된 닉네임입니다."),
//    DUPLICATE_ID("DI", HttpStatus.BAD_REQUEST, "중복된 ID입니다."),
//    DUPLICATE_TEL_NUMBER("DT", HttpStatus.BAD_REQUEST, "중복된 전화번호입니다."),
//    NOT_EXISTED_USER("NU", HttpStatus.BAD_REQUEST, "존재하지 않는 사용자입니다."),
//    NOT_EXISTED_BOARD("NB", HttpStatus.BAD_REQUEST, "존재하지 않는 게시글입니다."),
//    BAD_REQUEST("BD", HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
//    NOT_FOUND("NF", HttpStatus.BAD_REQUEST, "찾을 수 없습니다."),
    NOT_FOUND_PAGE("NFP", HttpStatus.BAD_REQUEST, "페이지를 찾을 수 없습니다."),

    // HTTP Status 401
//    SIGN_IN_FAIL("SF", HttpStatus.UNAUTHORIZED, "로그인 실패하였습니다."),
//    AUTHORIZATION_FAILED("AF", HttpStatus.UNAUTHORIZED, "인증에 실패하였습니다."),

    // HTTP Status 403
//    NO_PERMISSION("NP", HttpStatus.FORBIDDEN, "권한이 없습니다."),

    // HTTP Status 500
    DATABASE_ERROR("DBE", HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 오류입니다."),
    SERVER_ERROR("SER", HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;

    ResponseType(String code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

}

