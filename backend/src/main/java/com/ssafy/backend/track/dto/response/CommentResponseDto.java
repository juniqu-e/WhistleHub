package com.ssafy.backend.track.dto.response;

import com.ssafy.backend.member.model.common.MemberInfo;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponseDto {
    Integer commentId;
    MemberInfo memberInfo;
    String comment;
}
