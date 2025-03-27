package com.ssafy.backend.graph.service;

import com.ssafy.backend.graph.model.entity.MemberNode;
import com.ssafy.backend.graph.model.entity.TagNode;
import com.ssafy.backend.graph.model.entity.TrackNode;
import com.ssafy.backend.graph.model.entity.type.WeightType;
import com.ssafy.backend.graph.repository.MemberNodeRepository;
import com.ssafy.backend.graph.repository.RelationshipRepository;
import com.ssafy.backend.graph.repository.TagNodeRepository;
import com.ssafy.backend.graph.repository.TrackNodeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 데이터 수집 서비스
 * neo4j 데이터를 수집
 *
 * @author 박병주
 * @version 2.0
 * @since 2025-03-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataCollectingService {
    private final TrackNodeRepository trackNodeRepository;
    private final MemberNodeRepository memberNodeRepository;
    private final TagNodeRepository tagNodeRepository;
    private final RelationshipRepository relationshipRepository;

    /**
     * 회원가입 시 작성, 멤버 노드 생성
     *
     * @param memberId 생성할 회원의 id
     */
    @Transactional
    public void createMember(Integer memberId) {
        MemberNode memberNode = new MemberNode(memberId);
        memberNodeRepository.save(memberNode);
    }

    /**
     * 태그 노드를 생성
     *
     * @param tagId 생성할 태그의 id
     */
    @Transactional
    public void createTag(Integer tagId) {
        TagNode tagNode = new TagNode(tagId);
        tagNodeRepository.save(tagNode);
    }

    /**
     * 트랙 생성을 위한 메서드
     * 태그 관계도 같이 생성
     *
     * @param trackId 생성 트랙 id
     * @param tagIds 지정된 태그의 id가 포함된 List
     */
    @Transactional
    public void createTrack(Integer trackId, List<Integer> tagIds) {
        // 트랙 노드 생성
        TrackNode trackNode = new TrackNode(trackId);
        trackNodeRepository.save(trackNode);
        System.out.println("트랙 생성");
        // 태그와의 관계 생성
        for (Integer tagId : tagIds) {
            relationshipRepository.createHaveRelationship(trackId, tagId);
        }
    }

    /**
     * 회원이 트랙을 조회하면 조회수 가중치 증가
     *
     * @param memberId 조회하는 회원 id
     * @param trackId 조회되는 트랙 id
     * @param weightType 가중치 타입 EX) WeightType.VIEW
     */
    @Transactional
    public void viewTrack(Integer memberId, Integer trackId, WeightType weightType) {
        // 회원과 트랙 존재 확인
        if (!memberNodeRepository.existsById(memberId)) {
            log.warn("Member with id {} not found", memberId);
            throw new EntityNotFoundException("Member not found");
        }

        if (!trackNodeRepository.existsById(trackId)) {
            log.warn("Track with id {} not found", trackId);
            throw new EntityNotFoundException("Track not found");
        }

        // 가중치 타입에 따라 처리
        double weight = weightType.getValue();

        // LIKE 관계 생성 또는 가중치 증가
        memberNodeRepository.createOrIncreaseLikeRelationship(memberId, trackId, weight);

        // 트랙에 연결된 모든 태그 노드에 대해 PREFER 관계 생성 또는 가중치 증가
        List<Integer> tagIds = trackNodeRepository.findTagIdsByTrackId(trackId);
        for (Integer tagId : tagIds) {
            memberNodeRepository.createOrIncreasePreferRelationship(memberId, tagId, weight);
        }
    }

    /**
     * 회원이 트랙에 좋아요를 누를 때 가중치 증가
     *
     * @param memberId 좋아요를 누른 회원 id
     * @param trackId 좋아요가 눌린 트랙 id
     * @param weightType 가중치 타입 EX) WeightType.LIKE
     */
    @Transactional
    public void likeTrack(Integer memberId, Integer trackId, WeightType weightType) {
        // 회원과 트랙 존재 확인
        if (!memberNodeRepository.existsById(memberId)) {
            log.warn("Member with id {} not found", memberId);
            throw new EntityNotFoundException("Member not found");
        }

        if (!trackNodeRepository.existsById(trackId)) {
            log.warn("Track with id {} not found", trackId);
            throw new EntityNotFoundException("Track not found");
        }

        // 가중치 타입에 따라 처리
        double weight = weightType.getValue();

        // LIKE 관계 생성 또는 가중치 증가
        memberNodeRepository.createOrIncreaseLikeRelationship(memberId, trackId, weight);

        // 트랙에 연결된 모든 태그 노드에 대해 PREFER 관계 생성 또는 가중치 증가
        List<Integer> tagIds = trackNodeRepository.findTagIdsByTrackId(trackId);
        for (Integer tagId : tagIds) {
            memberNodeRepository.createOrIncreasePreferRelationship(memberId, tagId, weight);
        }
    }

    /**
     * 회원이 다른 회원을 팔로우
     *
     * @param followerId 팔로우하는 회원 id
     * @param followingId 팔로우 당하는 회원 id
     */
    @Transactional
    public void followMember(Integer followerId, Integer followingId) {
        // 회원 존재 확인
        if (!memberNodeRepository.existsById(followerId)) {
            log.warn("Follower with id {} not found", followerId);
            throw new EntityNotFoundException("Follower not found");
        }

        if (!memberNodeRepository.existsById(followingId)) {
            log.warn("Following with id {} not found", followingId);
            throw new EntityNotFoundException("Following not found");
        }

        // FOLLOW 관계 생성
        memberNodeRepository.createFollowRelationship(followerId, followingId);
    }

    /**
     * 트랙 간의 유사도 관계 생성
     *
     * @param trackId1 첫 번째 트랙 id
     * @param trackId2 두 번째 트랙 id
     */
    @Transactional
    public void createTrackSimilarity(Integer trackId1, Integer trackId2) {
        // 트랙 존재 확인
        if (!trackNodeRepository.existsById(trackId1)) {
            log.warn("Track with id {} not found", trackId1);
            throw new EntityNotFoundException("Track1 not found");
        }

        if (!trackNodeRepository.existsById(trackId2)) {
            log.warn("Track with id {} not found", trackId2);
            throw new EntityNotFoundException("Track2 not found");
        }

        // SIMILAR 관계 생성
        relationshipRepository.createSimilarRelationship(trackId1, trackId2);
    }

    /**
     * 회원이 트랙을 작성
     *
     * @param memberId 작성한 회원 id
     * @param trackId 작성된 트랙 id
     */
    @Transactional
    public void writeTrack(Integer memberId, Integer trackId) {
        // 회원과 트랙 존재 확인
        if (!memberNodeRepository.existsById(memberId)) {
            log.warn("Member with id {} not found", memberId);
            throw new EntityNotFoundException("Member not found");
        }

        if (!trackNodeRepository.existsById(trackId)) {
            log.warn("Track with id {} not found", trackId);
            throw new EntityNotFoundException("Track not found");
        }

        // WRITE 관계 생성
        relationshipRepository.createWriteRelationship(memberId, trackId);
    }
}
