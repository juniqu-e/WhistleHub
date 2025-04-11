package com.ssafy.backend.ai.sound.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SoundGenerationRequest {
    private String genre; // 음악 장르
    private String mood; // 분위기
    private double durationSeconds; // 음원 길이(초)
    private int tempo; // 템포(BPM)
    private String instruments; // 사용할 악기(콤마로 구분)
    private String additionalNotes; // 추가 요청사항
}
