package com.ssafy.backend.track.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@ToString
public class TrackImageUploadRequestDto {
    int trackId;
    MultipartFile trackImg;
}
