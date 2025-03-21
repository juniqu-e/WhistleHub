package com.ssafy.backend.playlist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetPlaylistTrackResponseDto {
    private int playlistTrackId;
    private int playOrder;
    private TrackInfo trackInfo;
}
