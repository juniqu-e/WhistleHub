package com.ssafy.backend.graph.service;

import com.ssafy.backend.graph.repository.MemberNodeRepository;
import com.ssafy.backend.graph.repository.TrackNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
    final private MemberNodeRepository memberNodeRepository;

    public List<Integer> getRecommendTrackIds(int memberId, int tagId, int trackCount) {
        return trackNodeRepository.getRecommendTrackIds(memberId, tagId, trackCount);
    }

    public List<Integer> getSimilarTrackIds(int trackId) {
        return trackNodeRepository.getSimilarTrackIds(trackId);
    }

    public List<Integer> getMemberFanMix(int memberId, int trackCount) {
        return trackNodeRepository.getMemberFanMix(memberId, trackCount);
    }

    public Integer getFanmixMemberId(int memberId, int size) {
        return memberNodeRepository.getFanmixMember(memberId, size);
    }
}

