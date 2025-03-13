package com.ssafy.whistlehub.graph.model.entity.relationship;

import com.ssafy.whistlehub.graph.model.entity.TagNode;
import com.ssafy.whistlehub.graph.model.entity.type.WeightType;
import lombok.Getter;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

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
public class WeightCount {
    @RelationshipId
    private Long id;

    private int weightCount;

    @TargetNode
    private final TagNode tag;

    public WeightCount(TagNode tag, int weightCount) {
        this.tag = tag;
        this.weightCount = weightCount;
    }

    /**
     * 관계에서 태그 선호도 가중치를 증가시키는 메소드
     */
    public void incrementWeight(WeightType weightType) {
        this.weightCount += weightType.getValue();
    }

}
