package com.ssafy.backend.auth.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <pre>로그인 응답 dto</pre>
 * 로그인 성공 시 응답할 데이터
 *
 * @author 허현준
 * @version 1.0
 * @since 2025-03-25
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {
    private String accessToken;
    private String refreshToken;
    private String profileImage;
    private String nickname;
}
