package com.ssafy.backend.graph.model.entity.relationship;

import com.ssafy.backend.graph.model.entity.TrackNode;
import com.ssafy.backend.graph.model.entity.type.WeightType;
import lombok.Getter;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

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
public class ViewCount {
    @RelationshipId
    private Long id;

    private int viewCount;

    @TargetNode
    private final TrackNode track;

    public ViewCount(TrackNode track, int viewCount) {
        this.track = track;
        this.viewCount = viewCount;
    }

    /**
     * 관계에서 트랙 조회수를 증가시키는 메소드
     */
    public void incrementCount(WeightType weightType) {
        this.viewCount += weightType.getValue();
    }
}
