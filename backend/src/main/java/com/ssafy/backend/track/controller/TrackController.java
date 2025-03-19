package com.ssafy.backend.track.controller;

import com.ssafy.backend.common.ApiResponse;
import com.ssafy.backend.track.dto.request.TrackUploadRequestDto;
import com.ssafy.backend.track.service.TrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TrackController {
    private final TrackService trackService;

    @GetMapping("/track/play")
    public ResponseEntity<Resource> trackPlay(int trackId) {
        byte[] file = trackService.trackPlay(trackId);
        ByteArrayResource resource = new ByteArrayResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audio.mp3\"")
                .body(resource);
    }

    @PostMapping("/track")
    public ApiResponse<?> createTrack(TrackUploadRequestDto trackUploadRequestDto) {
        System.out.println(trackUploadRequestDto);
        int memberId = 1;
        trackService.createTrack(trackUploadRequestDto, memberId);

        return new ApiResponse.builder<Object>()
                .payload("생성 성공")
                .build();
    }
}
