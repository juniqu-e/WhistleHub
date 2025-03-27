package com.ssafy.backend.graph.model.entity.relationship;

import com.ssafy.backend.graph.model.entity.TrackNode;
import com.ssafy.backend.graph.model.entity.type.WeightType;
import lombok.Getter;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import lombok.Setter;
import lombok.NoArgsConstructor;

/*
    @RelationshipProperties 어노테이션: 이 클래스가 관계의 속성을 나타냄을 지정
    @RelationshipId 어노테이션: 관계의 ID 필드를 표시
    @TargetNode 어노테이션: 관계의 끝 노드를 지정
 */

/**
 * <pre>관계 프로퍼티스 정의</pre>
 * 회원 -> 트랙 관계에서 viewCount 관리
 *
 * @author 박병주
 * @version 1.0
 * @since 2025-03-12
 */


@RelationshipProperties
@Getter
@Setter
public class LikeRelationship {
    @RelationshipId
    private Long id;

    private Double weight;

    @TargetNode
    private TrackNode trackNode;

    public LikeRelationship(TrackNode trackNode, Double weight) {
        this.trackNode = trackNode;
        this.weight = weight;
    }
}