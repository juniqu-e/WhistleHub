package com.ssafy.backend.member.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <pre>비밀번호 변경 요청 DTO</pre>
 *
 * 비밀번호 변경 요청을 위한 DTO 클래스입니다.
 * @author 허현준
 * @version 1.0
 * @since 2025-03-27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePasswordRequestDto {
    @NotBlank
    private String oldPassword;
    @NotBlank
    private String newPassword;
}
