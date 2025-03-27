package com.ssafy.backend.workstation.controller;

import com.ssafy.backend.common.ApiResponse;
import com.ssafy.backend.track.dto.request.TrackUploadRequestDto;
import com.ssafy.backend.track.service.TrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <pre>Track 컨트롤러</pre>
 *
 * @author 박병주
 * @version 1.0
 * @since 2025-03-18
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/workstation")
public class WorkstationController {
    private final TrackService trackService;
    @PostMapping()
    public ApiResponse<?> createTrack(TrackUploadRequestDto trackUploadRequestDto) {
        return new ApiResponse.builder<Object>()
                .payload(trackService.createTrack(trackUploadRequestDto))
                .build();
    }
}
