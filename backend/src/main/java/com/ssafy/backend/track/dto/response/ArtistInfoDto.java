package com.ssafy.backend.track.dto.response;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtistInfoDto {
    int memberId;
    String nickname;
    String profileImage;
}
