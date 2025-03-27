package com.ssafy.backend.auth.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

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
    @NotBlank
    private String loginId;
    @NotBlank
    private String password;
    @NotBlank
    private String email;
    @NotBlank
    private String nickname;
    @NotBlank
    private String birth;
    @NotBlank
    private Character gender;
    @NotBlank
    private List<Integer> tagList;
}
