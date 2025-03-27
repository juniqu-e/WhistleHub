package com.ssafy.backend.graph.repository;

import com.ssafy.backend.graph.model.entity.TagNode;
import com.ssafy.backend.graph.model.entity.TrackNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * <pre>Track 노드 repo</pre>
 *
 * @author 박병주
 * @version 1.0
 * @since 2025-03-12
 */

@Repository
public interface TrackNodeRepository extends Neo4jRepository<TrackNode, Integer> {
    @Query("MATCH (t:Track)-[:HAVE]-(tag:Tag) WHERE t.id = $trackId RETURN tag.id")
    List<Integer> findTagIdsByTrackId(@Param("trackId") Integer trackId);
}

