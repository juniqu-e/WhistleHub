package com.ssafy.backend.track.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TrackUpdateRequestDto {
    @NotBlank
    int trackId;
    @NotBlank
    String title;
    String description;
    @NotBlank
    boolean visibility;
}
