package com.ssafy.backend.member.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberDetailResponseDto {
    private String nickname;
    private String profileImage;
    private String profileText;
}
