package com.ssafy.backend.common.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ResponseType {
    // HTTP Status 200
    SUCCESS("SU", HttpStatus.OK, "요청이 성공적으로 처리되었습니다."),

    // HTTP Status 400
    NOT_FOUND_PAGE("NFP", HttpStatus.BAD_REQUEST, "페이지를 찾을 수 없습니다."),
    INVALID_EMAIL_AUTH("IEA", HttpStatus.BAD_REQUEST, "이메일 인증 코드가 올바르지 않습니다."),
    NOT_MATCH_ID_AND_EMAIL("NMIE", HttpStatus.BAD_REQUEST, "아이디 또는 이메일이 일치하지 않습니다."),
    INVALID_OLD_PASSWORD("IOP", HttpStatus.BAD_REQUEST, "기존 비밀번호가 올바르지 않습니다."),
    INVALID_NEW_PASSWORD_LENGTH("INPL", HttpStatus.BAD_REQUEST, "새로운 비밀번호의 길이가 너무 짧거나 깁니다."),
    UNREADABLE_IMAGE("URI", HttpStatus.BAD_REQUEST, "이미지를 읽을 수 없습니다."),
    NOT_FOLLOWING("NF", HttpStatus.BAD_REQUEST, "팔로우 취소를 요청했으나, 기존에 팔로우 관계가 아닙니다."),
    ALREADY_FOLLOWING("AF", HttpStatus.CONFLICT, "팔로우 추가를 요청했으나, 이미 팔로우 관계입니다."),
    INVALID_ORDER("IO", HttpStatus.BAD_REQUEST, "order 옵션이 잘못되었습니다."),

    // 필수 값 누락
    PARAMETER_REQUIRED("PR", HttpStatus.BAD_REQUEST, "필수 값이 누락되었습니다."),

    PLAYLIST_TITLE_REQUIRED("PTR", HttpStatus.BAD_REQUEST, "플레이리스트 제목이 없습니다."),

    // HTTP Status 401
    INVALID_CREDENTIALS("IC", HttpStatus.UNAUTHORIZED, "올바른 아이디 혹은 비밀번호가 아닙니다."),
    EMAIL_NOT_VERIFIED("ENV", HttpStatus.UNAUTHORIZED, "이메일 인증이 완료되지 않았습니다."),
    INVALID_REFRESH_TOKEN("IRT", HttpStatus.UNAUTHORIZED, "Refresh 토큰이 올바르지 않습니다."),
    EXPIRED_REFRESH_TOKEN("ERT", HttpStatus.UNAUTHORIZED, "Refresh 토큰이 만료되었습니다."),
    INVALID_ACCESS_TOKEN("IAT", HttpStatus.UNAUTHORIZED, "Access 토큰이 올바르지 않습니다."),
    EXPIRED_ACCESS_TOKEN("EAT", HttpStatus.UNAUTHORIZED, "Access 토큰이 만료되었습니다."),

    // HTTP Status 403

    // HTTP Status 404
    NOT_FOUND("NF", HttpStatus.NOT_FOUND, "해당 리소스를 찾을 수 없습니다."),
    NOT_FOUND_MEMBER("NFM", HttpStatus.NOT_FOUND, "찾는 회원이 없습니다."),
    FOLLOWER_NOT_FOUND("FNF", HttpStatus.NOT_FOUND, "찾는 팔로워가 없습니다."),
    TRACK_NOT_FOUND("TNF", HttpStatus.NOT_FOUND, "요청한 트랙이 없습니다."),
    PLAYLIST_NOT_FOUND("PNF", HttpStatus.NOT_FOUND, "요청한 플레이리스트가 없습니다."),

    // HTTP Status 409
    ALREADY_VALIDATED_EMAIL("AVE", HttpStatus.CONFLICT, "이미 인증된 이메일입니다."),
    DUPLICATE_NICKNAME("DNN", HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    DUPLICATE_ID("DID", HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다."),
    DUPLICATE_EMAIL("DEM", HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    DUPLICATE_TRACK("DT", HttpStatus.CONFLICT, "이미 존재하는 트랙입니다."),

    // HTTP Status 500
    DATABASE_ERROR("DBE", HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 오류입니다."),
    SERVER_ERROR("SER", HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),
    EMAIL_SEND_FAILED("ESF", HttpStatus.INTERNAL_SERVER_ERROR, "이메일 발송에 실패하였습니다."),
    IMAGE_UPLOAD_FAILED("IUF", HttpStatus.INTERNAL_SERVER_ERROR, "이미지를 업로드하는데 실패했습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;

    ResponseType(String code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

}
