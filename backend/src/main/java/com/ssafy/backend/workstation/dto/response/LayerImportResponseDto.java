package com.ssafy.backend.workstation.dto.response;

import lombok.*;

import java.util.List;

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
    List<Integer> bars;
}
