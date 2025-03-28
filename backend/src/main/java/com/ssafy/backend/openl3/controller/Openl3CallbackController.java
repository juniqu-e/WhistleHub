package com.ssafy.backend.openl3.controller;

import com.ssafy.backend.common.ApiResponse;
import com.ssafy.backend.common.FilterApiResponse;
import com.ssafy.backend.common.error.exception.MissingParameterException;
import com.ssafy.backend.graph.service.DataCollectingService;
import com.ssafy.backend.openl3.dto.SimilarCallbackDto;
import com.ssafy.backend.openl3.service.Openl3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/openl3")
@RequiredArgsConstructor
public class Openl3CallbackController {

    private final Openl3Service openl3Service;
    private final DataCollectingService dataCollectingService;

    @PostMapping("/similar/callback/{id}")
    public ApiResponse<?> handleCallback(@PathVariable int id, @RequestBody FilterApiResponse<List<SimilarCallbackDto>> request) {

        if(request.getPayload() == null) {
            log.error("Request body is null");
            throw new MissingParameterException();
        }

        log.info("Received callback with id: {}", id);
        log.info("Received callback with request: {}", request);

        //TODO: 그래프 db 저장 로직
//        [SimilarCallbackDto(trackId=0, similarity=1.0, distance=0.0), SimilarCallbackDto(trackId=0, similarity=1.0, distance=0.0), SimilarCallbackDto(trackId=0, similarity=1.0, distance=0.0), SimilarCallbackDto(trackId=0, similarity=0.0104, distance=94.6969), SimilarCallbackDto(trackId=0, similarity=0.0086, distance=114.7627), SimilarCallbackDto(trackId=0, similarity=0.0074, distance=133.7554), SimilarCallbackDto(trackId=0, similarity=0.0069, distance=144.3594), SimilarCallbackDto(trackId=0, similarity=0.0068, distance=145.2432), SimilarCallbackDto(trackId=0, similarity=0.0062, distance=160.7032), SimilarCallbackDto(trackId=0, similarity=0.0058, distance=172.2584)]

//        dataCollectingService.


        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }



}
