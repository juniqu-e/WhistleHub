package com.ssafy.backend.openl3.controller;

import com.ssafy.backend.common.ApiResponse;
import com.ssafy.backend.common.FilterApiResponse;
import com.ssafy.backend.common.error.exception.MissingParameterException;
import com.ssafy.backend.graph.service.DataCollectingService;
import com.ssafy.backend.openl3.dto.SimilarCallbackDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/openl3")
@RequiredArgsConstructor
public class Openl3CallbackController {

    private final DataCollectingService dataCollectingService;

    @PostMapping("/similar/callback/{id}")
    public ApiResponse<?> handleCallback(@PathVariable int id, @RequestBody FilterApiResponse<List<SimilarCallbackDto>> request) {

        if(request.getPayload() == null) {
            log.error("Request body is null");
            throw new MissingParameterException();
        }
        log.info("Received callback with id: {}", id);
        log.info("Received callback with request: {}", request);

        for(SimilarCallbackDto dto : request.getPayload()) {
            try{
                log.info("({})-[similarity:{}]->({})", id, dto.getSimilarity(), dto.getTrackId());
                dataCollectingService.createTrackSimilarity(id, dto.getTrackId(), dto.getSimilarity());
            } catch (Exception e) {
                log.error("Error processing callback for trackId: {}", dto.getTrackId(), e);
            }
        }

        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }
}