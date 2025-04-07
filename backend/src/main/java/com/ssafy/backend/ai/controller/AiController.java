package com.ssafy.backend.ai.controller;

import com.ssafy.backend.ai.service.SoundGeneratorService;
import com.ssafy.backend.ai.sound.dto.request.SoundGenerationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiController {

    final private SoundGeneratorService soundGeneratorService;

    @GetMapping("/generate")
    public ResponseEntity generate() {
        String genre = "jazz";
        String mood = "happy";
        int tempo = 115; //
        double durationSeconds = 4 * 4 * (60/(double)tempo);
        log.info("durationSeconds: {}", durationSeconds);
        String instruments = "piano";
        String additionalNotes = "none";

        SoundGenerationRequest request = new SoundGenerationRequest(genre, mood, durationSeconds, tempo, instruments, additionalNotes);

        InputStream audioStream = soundGeneratorService.generateSound(request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("audio/wav"));
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=generated_audio.wav");

        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(audioStream));
    }
}
