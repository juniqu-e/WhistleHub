package com.ssafy.backend.member.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestFollowRequestDto {
    @NotBlank
    private Integer memberId;
    @NotBlank
    private Boolean follow;
}
