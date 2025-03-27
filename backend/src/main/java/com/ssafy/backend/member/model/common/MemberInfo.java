package com.ssafy.backend.member.model.common;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberInfo {
    int memberId;
    String nickname;
    String profileImg;
}
