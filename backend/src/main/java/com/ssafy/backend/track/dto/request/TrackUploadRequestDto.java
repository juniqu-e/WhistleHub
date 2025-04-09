package com.ssafy.backend.track.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

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

    @NotBlank
    String[] layerName;

    @NotBlank
    int[] instrumentType;

    int bpm;
    String key;

    // 문자열로 받아서 처리
    private String barsJson;

    MultipartFile trackSoundFile;

    MultipartFile[] layerSoundFiles;

    MultipartFile trackImg;

    // JSON 문자열을 2차원 배열로 변환하는 메서드
    public int[][] getBars() {
        if (barsJson == null || barsJson.isEmpty()) {
            return new int[0][0];
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(barsJson, int[][].class);
        } catch (Exception e) {
            throw new RuntimeException("바 데이터 파싱 실패: " + e.getMessage(), e);
        }
    }
}
