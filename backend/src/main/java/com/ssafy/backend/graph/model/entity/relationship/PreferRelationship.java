package com.ssafy.backend.graph.model.entity.relationship;

import com.ssafy.backend.graph.model.entity.TagNode;
import lombok.Getter;
import org.springframework.data.neo4j.core.schema.*;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * <pre>관계 프로퍼티스 정의</pre>
 * 회원 -> 태그 관계에서 weightCount 관리
 *
 * @author 박병주
 * @version 1.0
 * @since 2025-03-12
 */



@RelationshipProperties
@Getter
@Setter
public class PreferRelationship {
    @RelationshipId
    private Long id;

    private Double weight;

    @TargetNode
    private TagNode tagNode;

    public PreferRelationship(TagNode tagNode, Double weight) {
        this.tagNode = tagNode;
        this.weight = weight;
    }
}
