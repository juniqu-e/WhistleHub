package com.ssafy.backend.openl3.controller;

import com.ssafy.backend.common.ApiResponse;
import com.ssafy.backend.common.error.exception.MissingParameterException;
import com.ssafy.backend.openl3.dto.SimilarCallbackDto;
import com.ssafy.backend.openl3.service.Openl3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/openl3")
@RequiredArgsConstructor
public class Openl3CallbackController {

    private final Openl3Service openl3Service;

    @PostMapping("/similar/callback")
    public ApiResponse<?> handleCallback(@RequestBody ApiResponse<List<SimilarCallbackDto>> request) {

        if(request.getBody() == null) {
            log.error("Request body is null");
            throw new MissingParameterException();
        }

        log.info(request.getBody().toString());

        //TODO: 그래프 db 저장 로직

        return new ApiResponse.builder<Object>()
                .payload(null)
                .build();
    }



}
