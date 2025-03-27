package com.ssafy.backend.graph.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.GeneratedValue;

/**
 * <pre>Tag 노드</pre>
 *
 * @author 박병주
 * @version 1.0
 * @since 2025-03-12
 */



@Node("Tag")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TagNode {
    @Id
    private Integer id;
}

