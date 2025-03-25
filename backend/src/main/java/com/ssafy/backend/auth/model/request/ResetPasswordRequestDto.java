package com.ssafy.backend.auth.model.request;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <pre>패스워드 재설정 요청 dto</pre>
 *
 * @author 허현준
 * @version 1.0
 * @since 2025-03-26
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResetPasswordRequestDto {
    @NotBlank
    private String email;
    @NotBlank
    private String loginId;
}
