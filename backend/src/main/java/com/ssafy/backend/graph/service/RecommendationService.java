package com.ssafy.backend.graph.service;

import com.ssafy.backend.graph.repository.MemberNodeRepository;
import com.ssafy.backend.graph.repository.TrackNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;

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

    /**
     * 추천 로직: 세 쿼리에서 각각 limit 개의 트랙 ID 목록을 가져온 후,
     * 중복된 아이디가 있을 경우 중복 횟수만큼 가중치를 부여하여 weighted random sampling을 진행합니다.
     *
     * @param memberId 사용자 아이디
     * @param tagId 태그 아이디
     * @param limit 추천할 트랙 수
     * @return 추천받은 트랙 id 목록
     */
    public List<Integer> getRecommendTrackIds(Integer memberId, Integer tagId, Integer limit) {
        // 각 쿼리의 결과 호출
        List<Integer> resultQuery1 = trackNodeRepository.getRecommendTrackIdsByMemberLikeSim(memberId, tagId, limit);
        List<Integer> resultQuery2 = trackNodeRepository.getRecommendTrackIdsByFollowingLike(memberId, tagId, limit);
        List<Integer> resultQuery3 = trackNodeRepository.getRecommendTrackIdsByFollowingLikeSim(memberId, tagId, limit);

        // 세 쿼리 결과를 하나의 리스트로 결합 (중복된 id는 그대로 포함됨)
        List<Integer> combinedList = new ArrayList<>();
        combinedList.addAll(resultQuery1);
        combinedList.addAll(resultQuery2);
        combinedList.addAll(resultQuery3);

        // 각 트랙 id의 발생 횟수를 카운트 (중복이 많을수록 높은 가중치 부여)
        Map<Integer, Integer> frequencyMap = new HashMap<>();
        for (Integer trackId : combinedList) {
            frequencyMap.put(trackId, frequencyMap.getOrDefault(trackId, 0) + 1);
        }

        // 중복 횟수를 가중치로 사용해 weighted random sampling without replacement을 진행
        return weightedRandomSample(frequencyMap, limit);
    }

    /**
     * 가중치 맵을 기반으로 sampling without replacement을 수행합니다.
     * 각 트랙 id는 frequencyMap에 기록된 가중치(중복 횟수)만큼 선택될 확률이 높아집니다.
     *
     * @param frequencyMap 키: 트랙 id, 값: 중복 횟수(가중치)
     * @param sampleSize 최종 선택할 아이디 개수
     * @return 선택된 트랙 id 목록
     */
    private List<Integer> weightedRandomSample(Map<Integer, Integer> frequencyMap, int sampleSize) {
        List<Integer> sampledIds = new ArrayList<>();
        Random random = new Random();

        // 가중치 맵이 빌 때까지 혹은 sampleSize만큼 뽑을 때까지 반복
        while (sampledIds.size() < sampleSize && !frequencyMap.isEmpty()) {
            int totalWeight = frequencyMap.values().stream().mapToInt(Integer::intValue).sum();
            // 1부터 totalWeight까지의 난수 발생 (난수 범위는 각 id의 누적 가중치와 비교)
            int randomWeight = random.nextInt(totalWeight) + 1;

            int cumulativeWeight = 0;
            Integer selectedId = null;
            // 가중치 누적하면서 난수보다 작거나 같은 id 선택
            for (Map.Entry<Integer, Integer> entry : frequencyMap.entrySet()) {
                cumulativeWeight += entry.getValue();
                if (randomWeight <= cumulativeWeight) {
                    selectedId = entry.getKey();
                    break;
                }
            }
            // 선택된 id가 있다면 결과에 추가한 후, 맵에서 제거 (중복 선택 방지)
            if (selectedId != null) {
                sampledIds.add(selectedId);
                frequencyMap.remove(selectedId);
            }
        }
        return sampledIds;
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

