package com.ssafy.backend.graph.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

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
@AllArgsConstructor
@NoArgsConstructor
public class MemberNode {
    @Id
    private Integer id;
}