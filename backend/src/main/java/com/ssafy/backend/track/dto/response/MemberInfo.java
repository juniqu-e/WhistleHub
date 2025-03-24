package com.ssafy.backend.track.dto.response;

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
