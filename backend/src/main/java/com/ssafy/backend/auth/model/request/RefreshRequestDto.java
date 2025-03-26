package com.ssafy.backend.auth.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <pre>토큰 재발급 요청 dto</pre>
 *
 * @author 허현준
 * @version 1.0
 * @since 2025-03-25
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RefreshRequestDto {
    @NotBlank
    private String refreshToken;
}
