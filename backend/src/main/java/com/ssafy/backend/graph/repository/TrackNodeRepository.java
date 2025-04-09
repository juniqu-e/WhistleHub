package com.ssafy.backend.graph.repository;

import com.ssafy.backend.graph.model.entity.TrackNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    @Query("""
            CALL {
              // 단계 1: 회원이 좋아한 트랙과 SIMILAR 관계의 트랙 (정렬 기준: 유사도)
              MATCH (m:Member {id: $memberId})-[:LIKE]->(liked:Track)
              MATCH (liked)-[similar:SIMILAR]->(rec:Track)
              MATCH (rec)-[:HAVE]->(:Tag {id: $tagId})
              RETURN rec.id AS trackId, 1 AS prio, similar.similarity AS orderValue
              UNION ALL
              // 단계 2: 회원이 팔로우한 회원이 좋아한 트랙 (정렬 기준: 좋아요 가중치)
              MATCH (m:Member {id: $memberId})-[:FOLLOW]->(other:Member)
              MATCH (other)-[like:LIKE]->(rec:Track)
              MATCH (rec)-[:HAVE]->(:Tag {id: $tagId})
              RETURN rec.id AS trackId, 2 AS prio, like.weight AS orderValue
              UNION ALL
              // 단계 3: 팔로우한 회원이 좋아한 트랙과 SIMILAR 관계의 트랙 (정렬 기준: 유사도)
              MATCH (m:Member {id: $memberId})-[:FOLLOW]->(other:Member)
              MATCH (other)-[:LIKE]->(liked:Track)
              MATCH (liked)-[similar:SIMILAR]->(rec:Track)
              MATCH (rec)-[:HAVE]->(:Tag {id: $tagId})
              RETURN rec.id AS trackId, 3 AS prio, similar.similarity AS orderValue
            }
            WITH trackId, prio, orderValue
            ORDER BY prio ASC, orderValue DESC
            RETURN trackId
            LIMIT $limit
            """)
    List<Integer> getRecommendTrackIds(@Param("memberId") Integer memberId, @Param("tagId") Integer tagId, @Param("limit") Integer limit);

    @Query("""
            MATCH (t:Track)-[similar:SIMILAR]->(other:Track)
            WHERE t.id = $trackId AND t.enabled = true AND other.enabled = true
            ORDER BY similar.similarity DESC
            RETURN other.id
            """)
    List<Integer> getSimilarTrackIds(@Param("trackId") Integer trackId);

    @Query("""
            MATCH (follower:Member)-[:FOLLOW]->(m:Member {id: $memberId})
            WHERE follower.enabled = true AND m.enabled = true
            MATCH (follower)-[like:LIKE]->(t:Track)
            WHERE t.enabled = true
            WITH t, max(like.weight) AS maxWeight
            RETURN t.id AS trackId
            ORDER BY maxWeight DESC
            LIMIT $limit
            """)
    List<Integer> getMemberFanMix(@Param("memberId") Integer memberId, @Param("limit") Integer limit);
}

