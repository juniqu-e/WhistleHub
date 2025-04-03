package com.ssafy.backend.discovery.controller;

import com.ssafy.backend.auth.model.common.TagDto;
import com.ssafy.backend.common.ApiResponse;
import com.ssafy.backend.common.error.exception.InvalidFormattedRequest;
import com.ssafy.backend.playlist.dto.TrackInfo;

import java.util.List;

import com.ssafy.backend.discovery.service.DiscoveryService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * <pre>Discovery Controller</pre>
 * 곡 발견 관련 API를 처리하는 컨트롤러.
 *
 * @author 허현준
 * @version 1.0
 * @since 2025-04-03
 */
@RestController
@RequestMapping("/api/discovery")
@RequiredArgsConstructor
@Slf4j
public class DiscoveryController {
    private final DiscoveryService discoveryService;

    /**
     * <pre>추천 태그 조회</pre>
     * Access Token의 멤버가 선호하는 순서의 태그 리스트 반환.
     *
     * @return 태그 리스트
     */
    @GetMapping("/tag")
    public ApiResponse<?> getPreferTag() {
        List<TagDto> result = discoveryService.getPreferTag();
        return new ApiResponse.builder<List<TagDto>>()
                .payload(result)
                .build();
    }

    /**
     * <pre>태그별 트랙 랭킹 조회</pre>
     *
     * @param period 랭킹 기간 (WEEK, MONTH)
     * @param tagId  태그 ID
     * @param page   페이지 번호
     * @param size   페이지 크기
     * @return 트랙 랭킹 리스트
     */
    @GetMapping("/tag/ranking")
    public ApiResponse<?> getTagRanking(@RequestParam(value = "period", required = true)
                                        @Pattern(regexp = "^(WEEK|MONTH)$")
                                        String period,
                                        @RequestParam(value = "tagId", required = true) Integer tagId,
                                        @RequestParam(value = "page", required = true)
                                        @Min(value = 0)
                                        Integer page,
                                        @RequestParam(value = "size", required = true)
                                        @Min(value = 0)
                                        Integer size) {

        List<TrackInfo> result = discoveryService.getTagRanking(tagId, period, PageRequest.of(page, size));

        return new ApiResponse.builder<List<TrackInfo>>()
                .payload(result)
                .build();
    }

    /**
     * <pre>추천 트랙 조회</pre>
     * Access Token의 멤버가 선호하는 태그에 따라 추천 트랙 리스트 반환.
     * @param tagId 태그 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 추천 트랙 리스트
     */
    @GetMapping("/tag/recommend")
    public ApiResponse<?> getTagRecommend(@RequestParam(value = "tagId", required = true) Integer tagId,
                                          @RequestParam(value = "page", required = true)
                                          @Min(value = 0)
                                          Integer page,
                                          @RequestParam(value = "size", required = true)
                                          @Min(value = 0)
                                          Integer size) {
        List<TrackInfo> result = discoveryService.getTagRecommend(tagId, PageRequest.of(page, size));

        return new ApiResponse.builder<List<TrackInfo>>()
                .payload(result)
                .build();
    }
}
