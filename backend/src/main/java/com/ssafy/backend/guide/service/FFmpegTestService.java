package com.ssafy.backend.guide.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FFmpegTestService {
    private static final Logger logger = LoggerFactory.getLogger(FFmpegTestService.class);

    private final FFmpeg ffmpeg;
    private final FFprobe ffprobe;

    private static final String SOURCE_DIR = "C:\\music";
    private static final String TARGET_DIR = "C:\\music\\trans";


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



//    ---------------
    /**
     * 디렉토리 내 모든 음원 파일의 처음 1분만 추출하여 저장합니다.
     * @return 처리된 파일 수
     */
    public int processAllAudioFiles() {
        try {
            // 대상 디렉토리가 없으면 생성
            Path targetPath = Paths.get(TARGET_DIR);
            if (!Files.exists(targetPath)) {
                Files.createDirectories(targetPath);
                logger.info("대상 디렉토리 생성: {}", TARGET_DIR);
            }

            // 소스 디렉토리의 모든 음원 파일 처리
            File sourceDir = new File(SOURCE_DIR);
            File[] audioFiles = sourceDir.listFiles((dir, name) ->
                    name.toLowerCase().endsWith(".mp3") ||
                            name.toLowerCase().endsWith(".wav") ||
                            name.toLowerCase().endsWith(".m4a") ||
                            name.toLowerCase().endsWith(".flac"));

            if (audioFiles == null || audioFiles.length == 0) {
                logger.warn("처리할 음원 파일이 없습니다.");
                return 0;
            }

            logger.info("총 {} 개의 음원 파일 처리 시작", audioFiles.length);
            int successCount = 0;

            for (File audioFile : audioFiles) {
                try {
                    if (extractFirstMinute(audioFile)) {
                        successCount++;
                    }
                } catch (Exception e) {
                    logger.error("파일 {} 처리 중 오류 발생: {}", audioFile.getName(), e.getMessage());
                }
            }

            logger.info("총 {}개 파일 중 {}개 처리 완료", audioFiles.length, successCount);
            return successCount;

        } catch (Exception e) {
            logger.error("파일 처리 중 오류 발생: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 단일 파일의 처음 1분만 추출합니다.
     * @param audioFile 처리할 오디오 파일
     * @return 성공 여부
     */
    public boolean extractFirstMinute(File audioFile) {
        try {
            String inputPath = audioFile.getAbsolutePath();
            String outputFilename = audioFile.getName().replaceFirst("[.][^.]+$", "") + "_1min"
                    + audioFile.getName().substring(audioFile.getName().lastIndexOf('.'));
            String outputPath = TARGET_DIR + File.separator + outputFilename;

            // FFmpeg 빌더 구성
            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(inputPath)
                    .addOutput(outputPath)
                    .setDuration(60, TimeUnit.SECONDS) // 1분(60초)만 추출
                    .setAudioCodec("copy") // 오디오 코덱 복사 (재인코딩 없음)
                    .done();

            // FFmpeg 실행
            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
            executor.createJob(builder).run();

            logger.info("파일 처리 완료: {} -> {}", audioFile.getName(), outputFilename);
            return true;
        } catch (Exception e) {
            logger.error("파일 {} 처리 중 오류 발생: {}", audioFile.getName(), e.getMessage());
            return false;
        }
    }

    /**
     * 지정된 경로의 단일 파일을 처리합니다.
     * @param filePath 파일 경로
     * @return 성공 여부
     */
    public boolean processSingleFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                logger.error("파일이 존재하지 않습니다: {}", filePath);
                return false;
            }
            return extractFirstMinute(file);
        } catch (Exception e) {
            logger.error("파일 처리 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }
}
