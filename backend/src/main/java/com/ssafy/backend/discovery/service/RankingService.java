package com.ssafy.backend.discovery.service;

import com.ssafy.backend.common.service.RedisService;
import com.ssafy.backend.discovery.model.common.TrackScore;
import com.ssafy.backend.mysql.entity.Tag;
import com.ssafy.backend.mysql.repository.RankingRepository;
import com.ssafy.backend.mysql.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <pre>랭킹 서비스</pre>
 * 랭킹 관련 비즈니스 로직을 처리하는 서비스 클래스이다.
 *
 * @author 허현준
 * @version 1.0
 * @since 2025-04-03
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {

    private static final int RANKING_COUNT = 50;
    private static final int PRIMARY_VIEW_WEIGHT = 2;
    private static final int PRIMARY_LIKE_WEIGHT = 4;

    private final RankingRepository rankingRepository;
    private final RedisService redisService;
    private final TagRepository tagRepository;

    /**
     * <pre>기간 시작일자</pre>
     * 기간에 따라 시작일자를 계산한다.
     *
     * @param period 기간
     * @return 시작일자
     */
    private LocalDateTime getPeriodStartDate(String period) {
        LocalDateTime now = LocalDateTime.now();
        return switch (period) {
            case "WEEK" -> now.minusWeeks(1);
            case "MONTH" -> now.minusMonths(1);
            default -> now;
        };
    }

    /**
     * <pre>랭킹 업데이트</pre>
     * tagId에 해당하는 태그의 랭킹을 업데이트한다.
     *
     * @param tagId  태그 ID
     * @param period 기간 (WEEK, MONTH)
     */
    public void doUpdatRanking(int tagId, String period) {
        period = period.toUpperCase();
        String startDate = getPeriodStartDate(period).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<TrackScore> results = rankingRepository.findRanking(tagId, startDate, RANKING_COUNT, PRIMARY_LIKE_WEIGHT, PRIMARY_VIEW_WEIGHT);

        // Redis Sorted Set 업데이트: 기존 키 삭제 후 새 데이터 추가
        String redisKey = getRedisKey(tagId, period);

        // Redis에 기존 데이터 삭제
        if (redisService.hasKey(redisKey))
            redisService.delete(redisKey);

        for (TrackScore result : results) {
            if(result.getScore() == 0) // 점수가 0인 경우는 제외
                continue;
            redisService.addSortedSet(redisKey, result.getTrackId().toString(), result.getScore());
        }
    }

    /**
     * <pre>랭킹 조회</pre>
     * tagId에 해당하는 태그의 랭킹을 조회한다.
     *
     * @param tagId       태그 ID
     * @param period      기간 (WEEK, MONTH)
     * @param pageRequest 페이지 요청
     * @return 랭킹 리스트
     */
    public List<Integer> getTagRanking(int tagId, String period, PageRequest pageRequest) {
        period = period.toUpperCase();
        String redisKey = getRedisKey(tagId, period);
        Set<Object> trackIds = redisService.getReverseRangeSortedSet(redisKey, pageRequest.getOffset(),
                pageRequest.getOffset() + pageRequest.getPageSize() - 1);
        if (trackIds == null) {
            return Collections.emptyList();
        }

        return trackIds.stream()
                .map(Object::toString)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    private String getRedisKey(int tagId, String period) {
        return String.format("ranking:%d:%s", tagId, period);
    }

    /**
     * <pre>전체 랭킹 업데이트</pre>
     * 전체 랭킹을 업데이트한다.
     * 매일 0시 실행 + 서버 재시작 시 실행
     */
    public void doUpdateAllRanking() {
        // 모든 태그에 대해 랭킹 업데이트
        log.info("Updating ranking");
        List<Tag> tags = tagRepository.findAll();
        for (Tag tag : tags) {
            doUpdatRanking(tag.getId(), "WEEK");
            doUpdatRanking(tag.getId(), "MONTH");
        }
        log.info("Ranking update completed");
    }
}

