package com.ssafy.backend.graph.repository;

import com.ssafy.backend.graph.model.entity.TagNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * <pre>Tag 노드 repo</pre>
 *
 * @author 박병주
 * @version 1.0
 * @since 2025-03-12
 */
public interface TagNodeRepository extends Neo4jRepository<TagNode, Integer> {
    Optional<TagNode> findByTagId(@Param("tagId") int tagId);

    @Query("MERGE (t:Tag {tagId: $tagId, name: $name}) RETURN t")
    void upsertTag(int tagId, String name);
}

