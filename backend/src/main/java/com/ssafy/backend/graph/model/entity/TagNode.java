package com.ssafy.backend.graph.model.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

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
@NoArgsConstructor
public class TagNode {
    @Id
    private int tagId;

    public TagNode(int tagId) {
        this.tagId = tagId;
    }

}

