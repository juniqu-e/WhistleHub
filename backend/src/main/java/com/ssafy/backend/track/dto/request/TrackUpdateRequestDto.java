package com.ssafy.backend.track.dto.request;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TrackUpdateRequestDto {
    int trackId;
    String title;
    String description;
    boolean visibility;
}
