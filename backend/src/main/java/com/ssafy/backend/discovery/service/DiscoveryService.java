package com.ssafy.backend.discovery.service;

import com.ssafy.backend.ai.service.Neo4jContentRetrieverService;
import com.ssafy.backend.auth.model.common.TagDto;
import com.ssafy.backend.auth.service.AuthService;
import com.ssafy.backend.common.error.exception.NotFoundException;
import com.ssafy.backend.common.error.exception.NotFoundMemberException;
import com.ssafy.backend.common.error.exception.NotFoundPageException;
import com.ssafy.backend.common.error.exception.NotFoundTrackException;
import com.ssafy.backend.graph.model.entity.TagNode;
import com.ssafy.backend.graph.service.RecommendationService;
import com.ssafy.backend.graph.service.RelationshipService;
import com.ssafy.backend.member.model.common.MemberInfo;
import com.ssafy.backend.mysql.entity.ListenRecord;
import com.ssafy.backend.mysql.entity.Member;
import com.ssafy.backend.mysql.entity.Tag;
import com.ssafy.backend.mysql.entity.Track;
import com.ssafy.backend.mysql.repository.*;
import com.ssafy.backend.playlist.dto.TrackInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <pre>Discovery 서비스</pre>
 * <p>
 * 곡 발견 관련 로직을 처리하는 클래스.
 *
 * @author 허현준
 * @version 1.0
 * @since 2025-04-03
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscoveryService {
    private final RelationshipService relationshipService;
    private final AuthService authService;
    private final TagRepository tagRepository;
    private final RecommendationService recommendationService;
    private final TrackRepository trackRepository;
    private final RankingService rankingService;
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final ListenRecoredRepository listenRecoredRepository;

    /**
     * <pre>좋아요 랭킹 조회</pre>
     *
     * @param period      기간
     * @param pageRequest 페이지 요청
     * @return 좋아요 랭킹 리스트
     */
    public List<TrackInfo> getTagRanking(int tagId, String period, PageRequest pageRequest) {
        List<Integer> likeRankingByTag = rankingService.getTagRanking(tagId, period, pageRequest);
        return getTrackInfoList(findTrackByIds(likeRankingByTag));
    }

    /**
     * <pre>선호 태그 조회</pre>
     *
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

    /**
     * <pre>추천 트랙 조회</pre>
     * Access Token의 멤버가 선호하는 태그에 따라 추천 트랙 리스트 반환.
     *
     * @param tagId 태그 ID
     * @param size  추천 트랙 갯수
     * @return 추천 트랙 리스트
     */
    public List<TrackInfo> getTagRecommend(int tagId, int size) {
        Member member = authService.getMember();

        // 추천 트랙을 가져온다.
        List<Integer> trackIds = recommendationService.getRecommendTrackIds(member.getId(), tagId, size);

        // 태그 노드가 존재하지 않는 경우
        // 주간 랭킹, 월간랭킹에서 추천 트랙을 가져온다.
        if (trackIds.isEmpty() || trackIds.size() < size) {
            List<Integer> weeklyRanking = rankingService.getTagRanking(
                    tagId,
                    "WEEK",
                    PageRequest.of(0, size - trackIds.size())
            );
            weeklyRanking.removeAll(trackIds);
            trackIds.addAll(weeklyRanking);
        }

        if (trackIds.isEmpty() || trackIds.size() < size) {
            List<Integer> monthlyRanking = rankingService.getTagRanking(
                    tagId,
                    "MONTH",
                    PageRequest.of(0, size - trackIds.size())
            );
            monthlyRanking.removeAll(trackIds);
            trackIds.addAll(monthlyRanking);
        }

        return getTrackInfoList(findTrackByIds(trackIds));
    }

    /**
     * <pre>최근 청취 트랙 조회</pre>
     *
     * @param size 최근 청취 트랙 갯수
     * @return 최근 청취 트랙 리스트
     */
    public List<TrackInfo> getRecentTrack(int size) {
        Member member = authService.getMember();
        List<ListenRecord> listenRecordList = listenRecoredRepository.findByMemberId(member.getId(), PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        List<Track> trackList = new ArrayList<>();
        for (ListenRecord listenRecord : listenRecordList) {
            trackList.add(listenRecord.getTrack());
        }

        return getTrackInfoList(trackList);
    }

    /**
     * <pre>유사 트랙 조회</pre>
     * 트랙 ID를 기반으로 유사한 트랙 리스트를 반환.
     *
     * @param trackId 트랙 ID
     * @return 유사 트랙 리스트
     */
    public List<TrackInfo> getSimilarTracks(int trackId) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> {
                    log.warn("Track not found with id: {}", trackId);
                    return new NotFoundTrackException();
                });

        List<Integer> similarTrackIds = recommendationService.getSimilarTrackIds(track.getId());

        return getTrackInfoList(findTrackByIds(similarTrackIds));
    }

    /**
     * <pre>한번도 들어보지 못한 음원</pre>
     * Access Token의 멤버가 한번도 들어보지 못한 음원 리스트를 반환.
     *
     * @param size 한번도 들어보지 못한 음원 갯수
     * @return 한번도 들어보지 못한 음원 리스트
     */
    public List<TrackInfo> getNeverListenTrack(int size) {
        Member member = authService.getMember();
        List<Track> trackList = trackRepository.findRandomTracksNotListenedByMember(member.getId(), size);
        return getTrackInfoList(trackList);
    }

    public MemberInfo getRandomFollowingMember(){
        Member member = authService.getMember();
        if(followRepository.countByFromMemberId(member.getId()) == 0){
            log.warn("No following members found for member id: {}", member.getId());
            return null;
        }

        Integer randomFollowing =  followRepository.findRandomFollowing(member.getId());
        if(randomFollowing == null){
            log.warn("No random following member found for member id: {}", member.getId());
            return null;
        }

        Member randomFollowingMember = memberRepository.findById(randomFollowing)
                .orElseThrow(()-> {
                    log.warn("Member not found with id: {}", randomFollowing);
                    return new NotFoundMemberException();
                });

        return MemberInfo.builder()
                .memberId(randomFollowingMember.getId())
                .nickname(randomFollowingMember.getNickname())
                .profileImg(randomFollowingMember.getProfileImage())
                .build();
    }

    public List<TrackInfo> getMemberFanMix(int memberId, int size) {
        List<Integer> trackIds = recommendationService.getMemberFanMix(memberId, size);

        return getTrackInfoList(findTrackByIds(trackIds));
    }

    private List<Track> findTrackByIds(List<Integer> trackIds) {
        List<Track> trackList = new ArrayList<>();
        for (Integer trackId : trackIds) {
            Track track = trackRepository.findById(trackId)
                    .orElseThrow(() -> {
                        log.warn("Track not found with id: {}", trackId);
                        return new NotFoundTrackException();
                    });
            trackList.add(track);
        }
        return trackList;
    }

    /**
     * <pre>트랙 정보 리스트 변환</pre>
     * 트랙 리스트를 TrackInfo 리스트로 변환.
     *
     * @param trackList TrackInfo로 만들 트랙 리스트
     * @return 변환된 TrackInfo 리스트
     */
    private List<TrackInfo> getTrackInfoList(List<Track> trackList) {
        List<TrackInfo> resultList = new ArrayList<>();
        for (Track track : trackList) {
            TrackInfo trackInfo = TrackInfo.builder()
                    .trackId(track.getId())
                    .title(track.getTitle())
                    .nickname(track.getMember().getNickname())
                    .imageUrl(track.getImageUrl())
                    .duration(track.getDuration())
                    .build();

            resultList.add(trackInfo);
        }

        return resultList;
    }
}
