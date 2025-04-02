package com.ssafy.backend.discovery.controller;

import com.ssafy.backend.auth.model.common.TagDto;
import com.ssafy.backend.common.ApiResponse;
import com.ssafy.backend.common.error.exception.InvalidFormattedRequest;
import com.ssafy.backend.playlist.dto.TrackInfo;

import java.util.List;

import com.ssafy.backend.discovery.service.DiscoveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/discovery")
@RequiredArgsConstructor
@Slf4j
public class DiscoveryController {
    private final DiscoveryService discoveryService;

    @GetMapping("/tag")
    public ApiResponse<?> getPreferTag() {
        List<TagDto> result = discoveryService.getPreferTag();
        return new ApiResponse.builder<List<TagDto>>()
                .payload(result)
                .build();
    }

    @GetMapping("/tag/ranking")
    public ApiResponse<?> getTagRanking(@RequestParam(value = "period", required = true, defaultValue = "WEEK") String period,
                                        @RequestParam(value = "tagId", required = true) Integer tagId,
                                        @RequestParam(value = "page", required = true, defaultValue = "0") Integer page,
                                        @RequestParam(value = "size", required = true, defaultValue = "10") Integer size) {
        if (!discoveryService.isValidPeriod(period)) {
            log.warn("Invalid period: {}", period);
            throw new InvalidFormattedRequest();
        }

        List<TrackInfo> result = discoveryService.getTagRanking(period, tagId, PageRequest.of(page, size));

        return new ApiResponse.builder<List<TrackInfo>>()
                .payload(result)
                .build();
    }

    @GetMapping("/tag/recommend")
    public ApiResponse<?> getTagRecommend(@RequestParam(value = "tagId", required = true) Integer tagId,
                                          @RequestParam(value = "size", required = true, defaultValue = "10") Integer size) {
        List<TrackInfo> result = discoveryService.getTagRecommend(tagId,size);

        return new ApiResponse.builder<List<TrackInfo>>()
                .payload(result)
                .build();
    }
}
