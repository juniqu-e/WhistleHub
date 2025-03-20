package com.ssafy.backend.track.dto.request;

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
    String title;
    String description;
    int duration;
    boolean visibility;
    Integer[] tags;
    Integer[] sourceTracks;
    List<LayerUploadRequestDto> layers;
    String[] layerName;
    String[] instrumentType;
    MultipartFile trackSoundFile;
    MultipartFile[] layerSoundFiles; //
    MultipartFile trackImg;
}
