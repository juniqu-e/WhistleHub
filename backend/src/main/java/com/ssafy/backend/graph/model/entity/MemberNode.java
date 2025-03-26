package com.ssafy.backend.graph.model.entity;

import com.ssafy.backend.graph.model.entity.relationship.ViewCount;
import com.ssafy.backend.graph.model.entity.relationship.WeightCount;
import com.ssafy.backend.graph.model.entity.type.WeightType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <pre>Member 노드</pre>
 *
 * @author 박병주
 * @version 1.0
 * @since 2025-03-12
 */

@Node("Member")
@Getter
@Setter
//@NoArgsConstructor
public class MemberNode {
    @Id
    private int memberId;

    @Relationship(type = "PREFERS", direction = Relationship.Direction.OUTGOING)
    private Set<ViewCount> preferredTrackNodes = new HashSet<>();

    @Relationship(type = "PREFERS", direction = Relationship.Direction.OUTGOING)
    private Set<WeightCount> preferredTagNodes = new HashSet<>();

    public MemberNode(int memberId) {
        this.memberId = memberId;
    }

    public void prefers(ViewCount viewCount) {
        preferredTrackNodes.add(viewCount);
    }

    public void prefers(WeightCount weightCount) {
        preferredTagNodes.add(weightCount);
    }

    /**
     * 멤버가 특정 트랙을 조회할 때 호출되는 메서드
     */
    public void viewTrack(TrackNode track, WeightType weightType, List<TagNode> connectedTags) {
        ViewCount viewCount = preferredTrackNodes.stream()
                .filter(vc -> vc.getTrack().getTrackId() == track.getTrackId())
                .findFirst()
                .orElse(null);

        if (viewCount == null) {
            viewCount = new ViewCount(track, weightType.getValue());
            prefers(viewCount);
        } else {
            viewCount.incrementCount(weightType);
        }

        // 트랙에 연결된 모든 태그 처리
        connectedTags.forEach(tag -> viewTag(tag, weightType));
    }

    public void viewTag(TagNode tag, WeightType weightType) {
        WeightCount weightCount = preferredTagNodes.stream()
                .filter(wc -> wc.getTag().getTagId() == tag.getTagId())
                .findFirst()
                .orElse(null);

        if (weightCount == null) {
            weightCount = new WeightCount(tag, weightType.getValue());
            prefers(weightCount);
        } else {
            weightCount.incrementWeight(weightType);
        }
    }
}



