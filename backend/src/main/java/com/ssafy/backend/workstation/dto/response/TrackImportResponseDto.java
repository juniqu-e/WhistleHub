package com.ssafy.backend.workstation.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackImportResponseDto {
    Integer trackId;
    String title;
    String imageUrl;
    String soundUrl;
    String key;
    int bpm;
    List<LayerImportResponseDto> layers;
}
