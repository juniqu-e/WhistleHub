package com.ssafy.backend.member.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <pre>회원 상세 정보 응답 DTO</pre>
 *
 * 회원의 닉네임, 프로필 이미지 URL, 프로필 텍스트를 포함하는 DTO 클래스입니다.
 * @author 허현준
 * @version 1.0
 * @since 2025-03-27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberDetailResponseDto {
    private String nickname;
    private String profileImage;
    private String profileText;
    private int followerCount;
    private int followingCount;
    private int trackCount;
}
