package com.ssafy.backend.playlist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadPlaylistImageRequestDto {
    private int playlistId;
    private String image;
}
