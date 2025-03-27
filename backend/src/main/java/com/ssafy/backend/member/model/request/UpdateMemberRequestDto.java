package com.ssafy.backend.member.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <pre>회원정보 수정 dto</pre>
 *
 * 회원정보 수정 요청을 받을 dto
 *
 * @author 허현준
 * @version 1.0
 * @since 2025.03.26
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMemberRequestDto {
    @NotBlank
    private String nickname;
    @NotBlank
    private String profileText;
}
