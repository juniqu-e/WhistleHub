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
    Integer[] importedLayers;
    List<LayerUploadRequestDto> layers;
    String[] layerName;
    String[] instrumentType;
    MultipartFile[] files; // 0번은 track 음원 1번부터 차례로 layer 음원
    MultipartFile trackImg;
}
