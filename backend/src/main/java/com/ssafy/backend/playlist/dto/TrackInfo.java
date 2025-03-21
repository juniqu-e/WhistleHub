package com.ssafy.backend.playlist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrackInfo {
    private int trackId;
    private String title;
    private String nickname;
    private int duration;
    private String imageUrl;
}
