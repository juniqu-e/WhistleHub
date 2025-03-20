package com.ssafy.backend.auth.model.request;

import lombok.*;

/**
 * <pre>회원가입 요청 dto</pre>
 *
 * @author 허현준
 * @version 1.0
 * @since 2025-03-20
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequestDto {
    private String loginId;
    private String password;
    private String email;
    private String nickname;
}
