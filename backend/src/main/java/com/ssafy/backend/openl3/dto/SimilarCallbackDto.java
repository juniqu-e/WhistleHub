package com.ssafy.backend.openl3.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimilarCallbackDto {
    private int trackId;
    private double similarity;
    private double distance;
}