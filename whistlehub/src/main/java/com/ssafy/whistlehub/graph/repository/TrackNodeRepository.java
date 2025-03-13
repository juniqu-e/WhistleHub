package com.ssafy.whistlehub.graph.repository;

import com.ssafy.whistlehub.graph.model.entity.TrackNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Optional;

/**
 * <pre>Track 노드 repo</pre>
 *
 * @author 박병주
 * @version 1.0
 * @since 2025-03-12
 */

public interface TrackNodeRepository extends Neo4jRepository<TrackNode, Integer> {
    Optional<TrackNode> findByTrackId(int trackId);
}

