package com.ssafy.backend.graph.service;

import com.ssafy.backend.graph.model.entity.TagNode;
import com.ssafy.backend.graph.repository.MemberNodeRepository;
import com.ssafy.backend.graph.repository.RelationshipRepository;
import com.ssafy.backend.graph.repository.TagNodeRepository;
import com.ssafy.backend.graph.repository.TrackNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RelationshipService {

    final private TrackNodeRepository trackNodeRepository;
    final private TagNodeRepository tagNodeRepository;
    final private MemberNodeRepository memberNodeRepository;
    final private RelationshipRepository relationshipRepository;

    /**
     * (Track1)-[SIM]->(Track2)
     * @param trackId1 track1
     * @param trackId2 track2
     * @param similarity 유사도 수치
     */
    @Transactional
    public void createSimilarRelationship(Integer trackId1, Integer trackId2, Double similarity) {
        relationshipRepository.createSimilarRelationship(trackId1, trackId2, similarity);
    }

    /**
     * (Track)-[HAVE]->(Tag)
     * @param trackId track
     * @param tagId tag
     */
    @Transactional
    public void createHaveRelationship(Integer trackId, Integer tagId) {
        relationshipRepository.createHaveRelationship(trackId, tagId);
    }

    /**
     * (Member)-[WRITE]->(Track)
     * @param memberId member
     * @param trackId track
     */
    @Transactional
    public void createWriteRelationship(Integer memberId, Integer trackId) {
        relationshipRepository.createWriteRelationship(memberId, trackId);
    }

    public List<Integer> getPreferTagsByMemberId(Integer memberId) {
        return relationshipRepository.findPreferTagsByMemberId(memberId);
    }

    /**
     * (Member)-[FOLLOW]->(Member)
     * @param followerId from
     * @param followingId to
     */
    @Transactional
    public void createFollowRelationship(Integer followerId, Integer followingId) {
        memberNodeRepository.createFollowRelationship(followerId, followingId);
    }

    @Transactional
    public void deleteFollowRelationship(Integer followerId, Integer followingId) {
        memberNodeRepository.deleteFollowRelationship(followerId, followingId);
    }

    @Transactional
    public void createLikeRelationship(Integer memberId, Integer trackId, Double weight) {
        memberNodeRepository.createLikeRelationship(memberId, trackId, weight);
    }

    @Transactional
    public void createPreferRelationship(Integer memberId, Integer tagId, Double weight) {
        memberNodeRepository.createPreferRelationship(memberId, tagId, weight);
    }

    /**
     * LIKE 관계를 생성하거나 이미 존재하는 경우 가중치를 증가시킵니다.
     */
    @Transactional
    public void createOrIncreaseLikeRelationship(Integer memberId, Integer trackId, Double weight) {
        memberNodeRepository.createOrIncreaseLikeRelationship(memberId, trackId, weight);
    }

    /**
     * PREFER 관계를 생성하거나 이미 존재하는 경우 가중치를 증가시킵니다.
     */
    @Transactional
    public void createOrIncreasePreferRelationship(Integer memberId, Integer tagId, Double weight) {
        memberNodeRepository.createOrIncreasePreferRelationship(memberId, tagId, weight);
    }

    /**
     * LIKE 관계가 존재하는지 확인합니다.
     */
    public boolean existsLikeRelationship(Integer memberId, Integer trackId) {
        return memberNodeRepository.existsLikeRelationship(memberId, trackId);
    }

    /**
     * PREFER 관계가 존재하는지 확인합니다.
     */
    public boolean existsPreferRelationship(Integer memberId, Integer tagId) {
        return memberNodeRepository.existsPreferRelationship(memberId, tagId);
    }

    @Transactional
    public void updateLikeWeight(Integer memberId, Integer trackId, Double newWeight) {
        memberNodeRepository.updateLikeWeight(memberId, trackId, newWeight);
    }

    @Transactional
    public void updatePreferWeight(Integer memberId, Integer tagId, Double newWeight) {
        memberNodeRepository.updatePreferWeight(memberId, tagId, newWeight);
    }

    @Transactional
    public void increasePreferWeight(Integer memberId, Integer tagId, Double increment) {
        memberNodeRepository.increasePreferWeight(memberId, tagId, increment);
    }

    @Transactional
    public void increaseLikeWeight(Integer memberId, Integer trackId, Double increment) {
        memberNodeRepository.increaseLikeWeight(memberId, trackId, increment);
    }
}