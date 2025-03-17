package com.ssafy.backend.guide.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FFmpegTestService {
    private final FFmpeg ffmpeg;
    private final FFprobe ffprobe;
    // 임시 파일 저장 경로
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    /**
     * MP3 파일을 2배속으로 변환
     */
    public byte[] speedUpMp3(MultipartFile file) throws IOException {
        // 원본 파일 저장
        String originalFilename = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID().toString();

        // 파일 경로 설정
        Path inputPath = Paths.get(TEMP_DIR, uniqueFileName + ".mp3");
        Path outputPath = Paths.get(TEMP_DIR, uniqueFileName + "_speed.mp3");

        try {
            // 임시 파일로 저장
            file.transferTo(inputPath.toFile());

            // FFmpeg 명령 구성
            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(inputPath.toString())
                    .addOutput(outputPath.toString())
                    .setAudioFilter("atempo=2.0") // 2배속 설정
                    .done();

            // FFmpeg 실행
            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
            executor.createJob(builder).run();

            // 변환된 파일 읽기
            byte[] processedBytes = Files.readAllBytes(outputPath);

            return processedBytes;
        } finally {
            // 임시 파일 삭제
            try {
                Files.deleteIfExists(inputPath);
                Files.deleteIfExists(outputPath);
            } catch (IOException e) {
                log.error("임시 파일 삭제 중 오류 발생", e);
            }
        }
    }
}
