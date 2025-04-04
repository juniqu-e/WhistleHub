package com.ssafy.backend.discovery.model.common;

import jakarta.persistence.Entity;
import lombok.*;

/**
 * <pre>트랙 점수</pre>
 * 트랙 ID와 점수를 저장하는 DTO, 랭킹 집계시 사용
 * @see com.ssafy.backend.discovery.service.RankingService
 * @see com.ssafy.backend.mysql.repository.RankingRepository
 * @see com.ssafy.backend.discovery.controller.DiscoveryController
 * @author 허현준
 * @version 1.0
 * @since 2025-04-03
 */

@Getter
@Setter
public class TrackScore {
    private Integer trackId;
    private Integer score;

    public TrackScore(Number trackId, Number score) {
        this.trackId = trackId != null ? trackId.intValue() : null;
        this.score = score != null ? score.intValue() : null;
    }
}