package com.ssafy.backend.graph.service;

import com.ssafy.backend.graph.model.entity.MemberNode;
import com.ssafy.backend.graph.model.entity.TagNode;
import com.ssafy.backend.graph.model.entity.TrackNode;
import com.ssafy.backend.graph.repository.MemberNodeRepository;
import com.ssafy.backend.graph.repository.TagNodeRepository;
import com.ssafy.backend.graph.repository.TrackNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NodeService {

    final private TrackNodeRepository trackNodeRepository;
    final private TagNodeRepository tagNodeRepository;
    final private MemberNodeRepository memberNodeRepository;

    @Transactional
    public void createTrackNode(Integer id) {
        TrackNode trackNode = new TrackNode();
        trackNode.setId(id);
        trackNode.setEnabled(true);
        trackNodeRepository.save(trackNode);
    }

    @Transactional
    public void createTagNode(Integer id) {
        TagNode tagNode = new TagNode();
        tagNode.setId(id);
        tagNodeRepository.save(tagNode);
    }

    @Transactional
    public void createMemberNode(Integer id) {
        MemberNode memberNode = new MemberNode();
        memberNode.setId(id);
        memberNode.setEnabled(true);
        memberNodeRepository.save(memberNode);
    }

    @Transactional
    public void deleteTrackNode(Integer id) {
        trackNodeRepository.findById(id).ifPresent(trackNode -> {
            trackNode.setEnabled(false);
            trackNodeRepository.save(trackNode);
        });
    }

    @Transactional
    public void deleteTagNode(Integer id) {
        tagNodeRepository.deleteById(id);
    }

    @Transactional
    public void deleteMemberNode(Integer id) {
        memberNodeRepository.findById(id).ifPresent(memberNode -> {
            memberNode.setEnabled(false);
            memberNodeRepository.save(memberNode);
        });
    }

    public Optional<TrackNode> findTrackNodeById(Integer id) {
        return trackNodeRepository.findById(id);
    }

    public Optional<TagNode> findTagNodeById(Integer id) {
        return tagNodeRepository.findById(id);
    }

    public Optional<MemberNode> findMemberNodeById(Integer id) {
        return memberNodeRepository.findById(id);
    }
}
