package com.ssafy.backend.auth.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <pre>토큰 재발급 응답 dto</pre>
 *
 * @author 허현준
 * @version 1.0
 * @since 2025-03-25
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshResponseDto {
    private String accessToken;
    private String refreshToken;
}
