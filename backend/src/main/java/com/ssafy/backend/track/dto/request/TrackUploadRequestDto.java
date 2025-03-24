package com.ssafy.backend.track.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
public class TrackUploadRequestDto {
    @NotBlank
    String title;
    @NotBlank
    String description;
    @NotBlank
    int duration;
    @NotBlank
    boolean visibility;
    Integer[] tags;
    Integer[] sourceTracks;
//    List<LayerUploadRequestDto> layers;
    @NotBlank
    String[] layerName;
    @NotBlank
    int[] instrumentType;
    MultipartFile trackSoundFile;
    MultipartFile[] layerSoundFiles; //
    MultipartFile trackImg;
}
