package com.ssafy.backend.guide;

import com.ssafy.backend.auth.service.AuthService;
import com.ssafy.backend.common.ApiResponse;
import com.ssafy.backend.common.error.exception.NotFoundPageException;
import com.ssafy.backend.graph.model.entity.type.WeightType;
import com.ssafy.backend.graph.service.DataCollectingService;
import com.ssafy.backend.graph.util.DataGenerator;
import com.ssafy.backend.guide.service.FFmpegTestService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;

@RequestMapping("/guide")
@RestController
@RequiredArgsConstructor
public class GuideController {

//    private final DataGeneratorService dataGeneratorService;
    private final DataCollectingService dataCollectingService;
    private final DataGenerator generator;
    private final FFmpegTestService ffmpegTestService;

    /**
     * MP3 파일을 업로드하고 2배속으로 변환하여 반환
     */
    @PostMapping("/speedup")
    public ResponseEntity<byte[]> speedUpAudio(@RequestParam("file") MultipartFile file) {
        try {
            // 파일 확장자 확인
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".mp3")) {
                return ResponseEntity.badRequest().body("MP3 파일만 업로드 가능합니다.".getBytes());
            }

            // 2배속 처리
            byte[] processedAudio = ffmpegTestService.speedUpMp3(file);

            // 응답 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
            headers.setContentDispositionFormData("attachment",
                    originalFilename.replace(".mp3", "_2x.mp3"));

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(processedAudio);

        } catch (IOException e) {
//            log.error("오디오 파일 처리 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/success")
    public ApiResponse<?> test(Authentication authentication) {
        return new ApiResponse.builder<Object>()
                .payload("데이터")
                .build();
    }

    private final AuthService authService;
    @GetMapping("/auth")
    public ApiResponse<?> getAuth(Authentication authentication) {

        return new ApiResponse.builder<Object>()
                .payload(authService.getMember())
                .build();
    }

    @GetMapping("/error")
    public void except(){
        throw new NotFoundPageException();
    }

    @GetMapping("/neo4j")
    public void neo4j() throws Exception {
//        dataGeneratorService.run();
    }

    @GetMapping("/create/member/{id}")
    public String createMember(@PathVariable int id) throws Exception {
        dataCollectingService.createMember(id);
        return "";
    }
    @GetMapping("/create/track/{id}")
    public String createTrack(@PathVariable int id) throws Exception {
        dataCollectingService.createTrack(id, Arrays.asList(1,2, 3, 4));
        return "";
    }

    @GetMapping("/view/{memberId}/{trackId}")
    public String view(@PathVariable int memberId, @PathVariable int trackId) throws Exception {
        dataCollectingService.viewTrack(memberId, trackId, WeightType.VIEW);
        return "";
    }

    @GetMapping("/tagging/{trackId}")
    public String tagging(@PathVariable int trackId) throws Exception {
        dataCollectingService.createTrack(trackId, Arrays.asList(1,2));
        return "";
    }

    @GetMapping("/create/dummy")
    public String createDummy() throws Exception {
        generator.gerating();
        return "";
    }

}
