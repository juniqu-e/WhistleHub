package com.ssafy.backend.common.prop;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * <pre>JWT 프로퍼티스 로딩</pre>
 * JWT 관련 프로퍼티스를 로딩하는 클래스
 *
 * @author 허현준
 * @version 1.0
 * @since 2025-03-26
 */

@Component
@Data
public class JWTProp {
    @Value("${JWT_ACCESS_EXPIRATION}")
    private Long ACCESS_TOKEN_EXPIRATION;

    @Value("${JWT_REFRESH_EXPIRATION}")
    private Long REFRESH_TOKEN_EXPIRATION;

}
