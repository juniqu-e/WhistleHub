package com.ssafy.backend.discovery.service;

import com.ssafy.backend.ai.service.Neo4jContentRetrieverService;
import com.ssafy.backend.auth.model.common.TagDto;
import com.ssafy.backend.auth.service.AuthService;
import com.ssafy.backend.common.error.exception.NotFoundException;
import com.ssafy.backend.common.error.exception.NotFoundPageException;
import com.ssafy.backend.graph.model.entity.TagNode;
import com.ssafy.backend.graph.service.RelationshipService;
import com.ssafy.backend.mysql.entity.Member;
import com.ssafy.backend.mysql.entity.Tag;
import com.ssafy.backend.mysql.entity.Track;
import com.ssafy.backend.mysql.repository.LikeRepository;
import com.ssafy.backend.mysql.repository.TagRepository;
import com.ssafy.backend.mysql.repository.TrackRepository;
import com.ssafy.backend.playlist.dto.TrackInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscoveryService {
    private final LikeRepository likeRepository;
    private final RelationshipService relationshipService;
    private final AuthService authService;
    private final TagRepository tagRepository;
    private final Neo4jContentRetrieverService neo4jContentRetrieverService;
    private final TrackRepository trackRepository;

    /**
     * <pre>좋아요 랭킹 조회</pre>
     *
     * @param period      기간
     * @param pageRequest 페이지 요청
     * @return 좋아요 랭킹 리스트
     */
    public List<TrackInfo> getTagRanking(String period, int tagId, PageRequest pageRequest) {
        String startDate = getPeriodStartDate(period).toString();
        List<TrackInfo> result = likeRepository.findLikeRankingByTagAndPeriod(tagId, startDate, pageRequest);
        if (result.isEmpty()) {
            log.warn("No ranking data found for period: {}", period);
            throw new NotFoundPageException();
        }

        return result;
    }

    /**
     * <pre>선호 태그 조회</pre>
     * @return 선호 태그 리스트
     */
    public List<TagDto> getPreferTag() {
        Member member = authService.getMember();
        List<Integer> preferTagNodeList = relationshipService.getPreferTagsByMemberId(member.getId());

        Set<Integer> preferTagIdSet = new HashSet<>();
        List<TagDto> resultList = new ArrayList<>();

        // 먼저 선호 태그들을 결과 리스트에 추가
        for (Integer tagIds : preferTagNodeList) {
            Tag tag = tagRepository.findById(tagIds)
                    .orElseThrow(() -> {
                        log.warn("Tag not found with id: {}", tagIds);
                        return new NotFoundException();
                    });
            // 선호 태그 ID를 Set에 추가하여 중복 방지
            preferTagIdSet.add(tag.getId());

            resultList.add(TagDto.builder()
                    .id(tag.getId())
                    .name(tag.getName())
                    .build());
        }

        // 선호도가 없는 나머지 태그들 추가
        List<Tag> allTags = tagRepository.findAll();
        for (Tag tag : allTags) {
            if (!preferTagIdSet.contains(tag.getId())) {
                resultList.add(TagDto.builder()
                        .id(tag.getId())
                        .name(tag.getName())
                        .build());
            }
        }

        return resultList;
    }

    public List<TrackInfo> getTagRecommend(int tagId, int size) {
        Member member = authService.getMember();

        List<Integer> trackIds = neo4jContentRetrieverService.retrieveTrackByMemberIdAndTagId(member.getId(), tagId, size);
        List<Track> trackList = trackRepository.findAllById(trackIds);

        List<TrackInfo> result = new ArrayList<>();

        for (Track track : trackList) {
            TrackInfo trackInfo = TrackInfo.builder()
                    .trackId(track.getId())
                    .title(track.getTitle())
                    .nickname(track.getMember().getNickname())
                    .imageUrl(track.getImageUrl())
                    .duration(track.getDuration())
                    .build();

            result.add(trackInfo);
        }
        return result;
    }
    /**
     * <pre>기간 시작일자</pre>
     * 기간에 따라 시작일자를 계산한다.
     *
     * @param period 기간
     * @return 시작일자
     */
    private LocalDateTime getPeriodStartDate(String period) {
        LocalDateTime now = LocalDateTime.now();
        switch (period) {
            case "WEEK":
                return now.minusWeeks(1);
            case "MONTH":
                return now.minusMonths(1);
            default:
                return now;
        }
    }

    /**
     * <pre>기간 유효성 검사</pre>
     * 기간이 null, 빈 문자열, "WEEK", "MONTH" 중 하나인지 확인한다.
     *
     * @param period 기간
     * @return 유효성 검사 결과
     */
    public boolean isValidPeriod(String period) {
        if (period == null || period.isEmpty()) {
            return false;
        }
        period = period.trim();
        period = period.toUpperCase();

        return period.equals("WEEK") || period.equals("MONTH");
    }
}
