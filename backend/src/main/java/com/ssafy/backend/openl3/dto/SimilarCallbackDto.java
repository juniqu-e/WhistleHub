package com.ssafy.backend.openl3.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimilarCallbackDto {
    @JsonProperty("track_id")
    private int trackId;
    private double similarity;
    private double distance;
}