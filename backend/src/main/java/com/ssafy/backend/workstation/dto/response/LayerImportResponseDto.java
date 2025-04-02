package com.ssafy.backend.workstation.dto.response;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LayerImportResponseDto {
    Integer layerId;
    Integer trackId;
    String name;
    Integer instrumentType;
    String soundUrl;
}
