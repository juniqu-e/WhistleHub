package com.ssafy.backend.graph.model.entity;

import lombok.*;
import org.springframework.data.neo4j.core.schema.*;

import java.util.HashSet;
import java.util.Set;

/**
 * <pre>Track 노드</pre>
 *
 * @author 박병주
 * @version 1.0
 * @since 2025-03-12
 */

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Node("Track")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrackNode {
    @Id
    private Integer id;
}

