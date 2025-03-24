package com.ssafy.backend.playlist.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetPlaylistResponseDto {
    private int memberId;
    private String name;
    private String description;
    private String imageUrl;
}
