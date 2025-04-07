package com.ssafy.backend.discovery.controller;

import com.ssafy.backend.auth.model.common.TagDto;
import com.ssafy.backend.common.ApiResponse;
import com.ssafy.backend.common.error.exception.InvalidFormattedRequest;
import com.ssafy.backend.member.model.common.MemberInfo;
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
     *
     * @param tagId 태그 ID
     * @param size  페이지 크기
     * @return 추천 트랙 리스트
     */
    @GetMapping("/tag/recommend")
    public ApiResponse<?> getTagRecommend(@RequestParam(value = "tagId", required = true) Integer tagId,
                                          @RequestParam(value = "size", required = true)
                                          @Min(value = 0)
                                          Integer size) {
        List<TrackInfo> result = discoveryService.getTagRecommend(tagId, size);

        return new ApiResponse.builder<List<TrackInfo>>()
                .payload(result)
                .build();
    }

    /**
     * <pre>최근 들은 음악 조회</pre>
     * Access Token의 멤버가 최근에 들은 음악 리스트 반환.
     *
     * @param size 음악 리스트 크기
     * @return 최근 들은 음악 리스트
     */
    @GetMapping("/recent")
    public ApiResponse<?> getRecentTrack(@RequestParam(value = "size", required = true)
                                         @Min(value = 0)
                                         Integer size) {
        List<TrackInfo> result = discoveryService.getRecentTrack(size);
        return new ApiResponse.builder<List<TrackInfo>>()
                .payload(result)
                .build();
    }

    @GetMapping("/recent/released")
    public ApiResponse<?> getRecentReleasedTrack(@RequestParam(value = "size", required = true)
                                                 @Min(value = 0)
                                                 Integer size) {
        List<TrackInfo> result = discoveryService.getRecentReleasedTrack(size);
        return new ApiResponse.builder<List<TrackInfo>>()
                .payload(result)
                .build();
    }

    /**
     * <pre>비슷한 트랙 조회</pre>
     *
     * @param trackId 트랙 ID
     * @return 비슷한 트랙 리스트
     */
    @GetMapping("/similar")
    public ApiResponse<?> getSimilarTracks(@RequestParam(value = "trackId", required = true)
                                           @Min(value = 0)
                                           Integer trackId) {
        List<TrackInfo> result = discoveryService.getSimilarTracks(trackId);
        return new ApiResponse.builder<List<TrackInfo>>()
                .payload(result)
                .build();
    }

    /**
     * <pre>한번도 들어보지 않은 트랙 조회</pre>
     *
     * @param size 조회할 트랙의 수
     * @return 한번도 들어보지 않은 트랙 리스트
     */
    @GetMapping("/never")
    public ApiResponse<?> getNeverListenTrack(@RequestParam(value = "size", required = true)
                                              @Min(value = 0)
                                              Integer size) {
        List<TrackInfo> result = discoveryService.getNeverListenTrack(size);
        return new ApiResponse.builder<List<TrackInfo>>()
                .payload(result)
                .build();
    }

    /**
     * <pre>팔로우한 회원중 랜덤한 회원 조회</pre>
     * Access Token의 곡을 만들어 올린 팔로잉 회원 중 랜덤한 회원을 조회.
     *
     * @return 팔로우한 회원의 트랙 리스트
     */
    @GetMapping("/fanmix/following")
    public ApiResponse<?> getRandomFollowingMember() {
        MemberInfo result = discoveryService.getRandomFollowingMember();
        return new ApiResponse.builder<MemberInfo>()
                .payload(result)
                .build();
    }

    /**
     * <pre>특정 멤버의 팔로워들이 좋아하는 트랙 조회</pre>
     *
     * @param memberId 어떤 팔로워들이 좋아하는 트랙을 조회할 멤버 ID
     * @param size     조회할 트랙의 수
     * @return 특정 멤버의 팔로워들이 좋아하는 트랙 리스트
     */
    @GetMapping("/fanmix")
    public ApiResponse<?> getMemberFanMix(@RequestParam(value = "memberId", required = true)
                                          @Min(value = 0)
                                          Integer memberId,
                                          @RequestParam(value = "size", required = true)
                                          @Min(value = 0)
                                          Integer size) {
        List<TrackInfo> result = discoveryService.getMemberFanMix(memberId, size);
        return new ApiResponse.builder<List<TrackInfo>>()
                .payload(result)
                .build();
    }
}
