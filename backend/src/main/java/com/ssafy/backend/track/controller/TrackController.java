package com.ssafy.backend.track.controller;

import com.ssafy.backend.auth.service.AuthService;
import com.ssafy.backend.common.ApiResponse;
import com.ssafy.backend.track.dto.request.TrackImageUploadRequestDto;
import com.ssafy.backend.track.dto.request.TrackUpdateRequestDto;
import com.ssafy.backend.track.dto.request.TrackUploadRequestDto;
import com.ssafy.backend.track.service.TrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * <pre>Track 컨트롤러</pre>
 *
 * @author 박병주
 * @version 1.0
 * @since 2025-03-18
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TrackController {
    private final TrackService trackService;
    private final AuthService authService;


    @GetMapping("/track")
    public ApiResponse<?> viewTrack(int trackId) {
        return new ApiResponse.builder<Object>()
                .payload(trackService.viewTrack(trackId, authService.getMember().getId()))
                .build();
    }
    @PutMapping("/track")
    public ApiResponse<?> updateTrack(TrackUpdateRequestDto trackUpdateRequestDto) {
        trackService.updateTrack(trackUpdateRequestDto, authService.getMember().getId());
        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }

    @GetMapping("/track/play")
    public ResponseEntity<Resource> trackPlay(int trackId) {
        byte[] file = trackService.trackPlay(trackId);
        ByteArrayResource resource = new ByteArrayResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audio.mp3\"")
                .body(resource);
    }

    @PostMapping("/track/image")
    public ApiResponse<?> uploadTrackImage(TrackImageUploadRequestDto trackImageUploadRequestDto) {
        return new ApiResponse.builder<Object>()
                .payload(trackService.updateImage(trackImageUploadRequestDto))
                .build();
    }

    @DeleteMapping("/track")
    public ApiResponse<?> deleteTrack(int trackId) {
        trackService.deleteTrack(trackId);
        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }

    @PostMapping("/workstation")
    public ApiResponse<?> createTrack(TrackUploadRequestDto trackUploadRequestDto) {
        return new ApiResponse.builder<Object>()
                .payload(trackService.createTrack(trackUploadRequestDto, authService.getMember().getId()))
                .build();
    }
}
