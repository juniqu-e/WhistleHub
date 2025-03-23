package com.ssafy.backend.track.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackResponseDto {
    Integer trackId;
    String title;
    String description;
    String imageUrl;
    ArtistInfoDto artist;
    Boolean isLiked;
    Integer importCount;
    Integer likeCount;
    Integer viewCount;
    String createdAt;
    List<TrackInfoDto> sourceTracks;
    List<TrackInfoDto> importTracks;
    List<TagInfo> tags;
}
