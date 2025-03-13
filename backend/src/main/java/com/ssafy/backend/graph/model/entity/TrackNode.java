package com.ssafy.backend.graph.model.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

/**
 * <pre>Track 노드</pre>
 *
 * @author 박병주
 * @version 1.0
 * @since 2025-03-12
 */

@Node("Track")
@Getter
@Setter
public class TrackNode {
    @Id
    private int trackId;

    @Relationship(type = "HAS_TAG", direction = Relationship.Direction.OUTGOING)
    private Set<TagNode> tagNodes = new HashSet<>();

    public TrackNode(int trackId){
        this.trackId = trackId;
    }

    public void addTag(TagNode tagNode) {
        tagNodes.add(tagNode);
    }
}

