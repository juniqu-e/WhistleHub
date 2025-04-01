package com.ssafy.backend.track.controller;

import com.ssafy.backend.auth.service.AuthService;
import com.ssafy.backend.common.ApiResponse;
import com.ssafy.backend.track.dto.request.*;
import com.ssafy.backend.track.service.TrackCommentService;
import com.ssafy.backend.track.service.TrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <pre>Track 컨트롤러</pre>
 *
 * @author 박병주
 * @version 1.0
 * @since 2025-03-18
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/track")
public class TrackController {
    private final TrackService trackService;
    private final TrackCommentService trackCommentService;
    private final AuthService authService;


    @GetMapping()
    public ApiResponse<?> viewTrack(int trackId) {
        return new ApiResponse.builder<Object>()
                .payload(trackService.viewTrack(trackId))
                .build();
    }

    @PutMapping()
    public ApiResponse<?> updateTrack(@RequestBody TrackUpdateRequestDto trackUpdateRequestDto) {
        trackService.updateTrack(trackUpdateRequestDto);
        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }

    @PostMapping("/image")
    public ApiResponse<?> uploadTrackImage(TrackImageUploadRequestDto trackImageUploadRequestDto) {
        return new ApiResponse.builder<Object>()
                .payload(trackService.updateImage(trackImageUploadRequestDto))
                .build();
    }

    @DeleteMapping()
    public ApiResponse<?> deleteTrack(int trackId) {
        trackService.deleteTrack(trackId);
        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }

    @GetMapping("/play")
    public ResponseEntity<Resource> trackPlay(int trackId) {
        byte[] file = trackService.trackPlay(trackId);
        ByteArrayResource resource = new ByteArrayResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audio.wav\"")
                .body(resource);
    }

    @PostMapping("/play")
    public ApiResponse<?> recordPlay(@RequestBody Map<String, Integer> request) {
        trackService.recordPlay(request.get("trackId"));
        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }

    @GetMapping("/layer")
    public ApiResponse<?> getTrackLayers(int trackId) {
        return new ApiResponse.builder<Object>()
                .payload(trackService.getLayers(trackId))
                .build();
    }

    @GetMapping("/layer/play")
    public ResponseEntity<Resource> layerPlay(int layerId) {
        byte[] file = trackService.layerPlay(layerId);
        ByteArrayResource resource = new ByteArrayResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audio.wav\"")
                .body(resource);
    }

    @PostMapping("/like")
    public ApiResponse<?> likeTrack(@RequestBody Map<String, Integer> request) {
        trackService.likeTrack(request.get("trackId"));
        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }

    @DeleteMapping("/like")
    public ApiResponse<?> unlikeTrack(int trackId) {
        trackService.unlikeTrack(trackId);
        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }

    @GetMapping("/comment")
    public ApiResponse<?> getTrackComments(int trackId) {
        return new ApiResponse.builder<Object>()
                .payload(trackCommentService.getComments(trackId))
                .build();
    }
    @PostMapping("/comment")
    public ApiResponse<?> insertTrackComment(@RequestBody TrackCommentRequresDto trackCommentRequresDto) {
        trackCommentService.insertTrackComment(trackCommentRequresDto.getTrackId(), trackCommentRequresDto.getContext());
        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }
    @PutMapping("/comment")
    public ApiResponse<?> updateTrackComment(@RequestBody TrackCommentUpdateRequestDto trackCommentUpdateRequestDto) {
        trackCommentService.updateTrackComment(trackCommentUpdateRequestDto.getCommentId(), trackCommentUpdateRequestDto.getContext());
        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }
    @DeleteMapping("/comment")
    public ApiResponse<?> deleteTrackComment(int commentId) {
        trackCommentService.deleteTrackComment(commentId);
        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }

    @PostMapping("/report")
    public ApiResponse<?> reportTrack(@RequestBody TrackReportRequestDto trackReportRequestDto) {
        trackService.reportTrack(trackReportRequestDto.getTrackId(), trackReportRequestDto.getType(), trackReportRequestDto.getDetail());
        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<?> getSearchTrack(String keyword,
                                   int page,
                                   int size,
                                   String orderBy) {
        return new ApiResponse.builder<Object>()
                .payload(trackService.searchTrack(keyword, page, size, orderBy))
                .build();
    }

}
