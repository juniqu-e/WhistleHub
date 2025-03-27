package com.ssafy.backend.track.dto.response;

import lombok.*;

import java.util.List;

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
