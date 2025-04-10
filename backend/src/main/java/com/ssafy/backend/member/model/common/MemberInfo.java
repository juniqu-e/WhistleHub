package com.ssafy.backend.member.model.common;

import lombok.*;

/**
 * <pre>회원 정보</pre>
 *
 * 회원의 ID, 닉네임, 프로필 이미지 URL을 포함하는 클래스입니다.
 * @author 박병주
 * @version 1.0
 * @since 2025-03-24
 */
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
