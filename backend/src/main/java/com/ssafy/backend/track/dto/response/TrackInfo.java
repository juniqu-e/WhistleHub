package com.ssafy.backend.track.dto.response;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackInfo {
    int trackId;
    String title;
    int duration;
    String imageUrl;
}
