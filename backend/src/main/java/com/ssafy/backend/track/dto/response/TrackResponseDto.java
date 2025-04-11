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
    int duration;
    String imageUrl;
    ArtistInfoDto artist;
    Boolean isLiked;
    Integer importCount;
    Integer likeCount;
    Integer viewCount;
    String createdAt;
    String key;
    int bpm;
    List<TrackInfo> sourceTracks;
    List<TrackInfo> importTracks;
    List<TagInfo> tags;
}
