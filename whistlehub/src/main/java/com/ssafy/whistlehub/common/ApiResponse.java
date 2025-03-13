package com.ssafy.whistlehub.common;

import com.ssafy.whistlehub.common.error.ResponseType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>API 공통 반환 객체</pre>
 *
 * @author 박병주
 * @version 1.0
 * @since 2025-03-12
 */

public class ApiResponse<T> extends ResponseEntity<T> {
    // 생성자
    public ApiResponse(T payload, HttpStatus status) {
        super(payload, status);
    }

    // 내부 빌더 클래스
    public static class builder<T> {
        private String message = ResponseType.SUCCESS.getMessage();
        private String code = ResponseType.SUCCESS.getCode();
        private HttpStatus status = ResponseType.SUCCESS.getStatus();
        private T payload;

        // 메세지 설정
        public builder<T> message(String message) {
            this.message = message;
            return this;
        }

        // 코드 설정 (HTTP status code 사용, 존재하지 않는 경우 OK로 기본 처리)
        public builder<T> code(String code) {
            this.code = code;
            return this;
        }

        public builder<T> object(T payload) {
            this.payload = payload;
            return this;
        }

        public builder<T> errorStatus(ResponseType responseType) {
            this.message = responseType.getMessage();
            this.code = responseType.getCode();
            this.status = responseType.getStatus();
            return this;
        }

        // 빌드 메소드: 메시지와 코드를 담은 ResponseEntity 생성
        public ApiResponse<Map<String, Object>> build() {
            Map<String, Object> body = new HashMap<>();
            body.put("message", message);
            body.put("code", code);
            body.put("payload", payload);

            if (status == null) {
                status = HttpStatus.OK;
            }

            return new ApiResponse<>(body, status);
        }
    }

}
