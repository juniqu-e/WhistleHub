package com.ssafy.backend.graph.service;

import com.ssafy.backend.graph.model.entity.MemberNode;
import com.ssafy.backend.graph.model.entity.TagNode;
import com.ssafy.backend.graph.model.entity.TrackNode;
import com.ssafy.backend.graph.model.entity.type.WeightType;
import com.ssafy.backend.graph.repository.MemberNodeRepository;
import com.ssafy.backend.graph.repository.TagNodeRepository;
import com.ssafy.backend.graph.repository.TrackNodeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <pre>데이터 수집 서비스</pre>
 * neo4j 데이터를 수집
 * @author 박병주
 * @version 1.0
 * @since 2025-03-12
 */
@Service
@RequiredArgsConstructor
public class DataCollectingService {
    private final TrackNodeRepository trackNodeRepository;
    private final MemberNodeRepository memberNodeRepository;
    private final TagNodeRepository tagNodeRepository;

    /**
     * 회원가입 시 작성, 멤버 노드 생성
     * @param memberId 생성할 회원의 id
     */
    @Transactional
    public void createMember(int memberId){
        memberNodeRepository.save(new MemberNode(memberId));
    }

    /**
     * TODO: 태그 생성은 Tag generator로 생성 관리 예정
     * 태그 노드를 생성
     * @param tagId 생성할 태그의 id
     * @param tagName 생성할 태그의 이름
     */
    @Transactional
    public void createTag(int tagId, String tagName){
        tagNodeRepository.upsertTag(tagId, tagName);
    }

    /**
     * 회원이 트랙을 조회하면 조회수 가중치 증가
     * @param memberId 조회하는 회원 id
     * @param trackId 조회되는 트랙 id
     * @param weightType 가중치 타입 EX)<code>WeightType.VIEW</code>
     */
    @Transactional
    public void viewTrack(int memberId, int trackId, WeightType weightType, List<Integer> tagsId) {
        MemberNode member = memberNodeRepository.findByMemberId(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));
        TrackNode track = trackNodeRepository.findByTrackId(trackId)
                .orElseThrow(() -> new EntityNotFoundException("Track not found"));
        member.viewTrack(track, weightType, tagsId);

        memberNodeRepository.save(member);
    }

    /**
     * <pre>트랙 생성을 위한 메서드</pre>
     * 태그 관계도 같이 생성
     * @param trackId 생성 트랙 id
     * @param tagIds 지정된 태그의 id가 포함된 List
     */
    @Transactional
    public void createTrack(int trackId, List<Integer> tagIds) {
        TrackNode track = new TrackNode(trackId);
        tagIds.forEach(tagId -> {
            TagNode tagNode = tagNodeRepository.findByTagId(tagId)
                    .orElseGet(() -> tagNodeRepository.save(new TagNode(tagId)));
            track.addTag(tagNode);
        });
        trackNodeRepository.save(track);
    }

}
