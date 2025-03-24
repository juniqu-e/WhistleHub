package com.ssafy.backend.track.dto.response;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LayerResponseDto {
    int layerId;
    String name;
    int instrumentType;
}
