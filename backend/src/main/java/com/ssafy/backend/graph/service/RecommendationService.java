package com.ssafy.backend.graph.service;

import com.ssafy.backend.graph.repository.TrackNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <pre>추천 데이터 제공 서비스</pre>
 * neo4j 데이터를 활용해 추천 데이터를 가공하는 서비스
 *
 * @author 허현준
 * @version 2.0
 * @since 2025-04-03
 */

@Service
@RequiredArgsConstructor
public class RecommendationService {
    final private TrackNodeRepository trackNodeRepository;

    public List<Integer> getRecommendTrackIds(int memberId, int tagId, int trackCount) {
        return trackNodeRepository.getRecommendTrackIds(memberId, tagId, trackCount);
    }
}

