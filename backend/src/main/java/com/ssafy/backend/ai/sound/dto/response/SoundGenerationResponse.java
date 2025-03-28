package com.ssafy.backend.ai.sound.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoundGenerationResponse {
    private String fileName; // 생성된 파일명
    private String downloadUrl; // 다운로드 URL
    private String mimeType; // 파일 MIME 타입
    private long fileSize; // 파일 크기(바이트)
    private int durationSeconds; // 음원 길이(초)
    private String generatedAt; // 생성 시간
    private String aiFeedback; // AI 피드백 또는 메모
}
