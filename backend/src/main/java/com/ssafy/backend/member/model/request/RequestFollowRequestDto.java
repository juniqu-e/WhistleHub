package com.ssafy.backend.member.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * <pre>팔로우 요청 DTO</pre>
 *
 * 팔로우 요청을 위한 DTO 클래스입니다.
 * @author 허현준
 * @version 1.0
 * @since 2025-03-27
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestFollowRequestDto {
    @NotBlank
    private Integer memberId;
    @NotBlank
    private Boolean follow;
}
