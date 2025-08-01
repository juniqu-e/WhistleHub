package com.ssafy.backend.track.dto.response;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackSearchResponseDto {
    int trackId;
    String title;
    String nickname;
    int duration;
    String imageUrl;
    String soundUrl;
}
