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
 * @author 허현준
 * @version 1.0
 * @since 2025-03-12
 */

@Repository
public interface TrackNodeRepository extends Neo4jRepository<TrackNode, Integer> {
    @Query("MATCH (t:Track)-[:HAVE]-(tag:Tag) WHERE t.id = $trackId RETURN tag.id")
    List<Integer> findTagIdsByTrackId(@Param("trackId") Integer trackId);


    @Query("""
            MATCH (m:Member {id: $memberId})-[like:LIKE]->(liked:Track)-[similar:SIMILAR]->(rec:Track)-[:HAVE]->(:Tag {id: $tagId})
           WHERE liked.enabled = true AND m.enabled = true
                        WITH liked, like.weight AS weight
                        MATCH (liked)-[similar:SIMILAR]->(rec:Track)-[:HAVE]->(:Tag {id: $tagId})
                        WHERE rec.enabled = true
                        WITH liked, rec, similar.similarity AS sim, weight
                        ORDER BY sim DESC
                        // 각 liked 노드별로 similar.similarity 기준 상위 3개의 rec만 컬렉션에 담음
                        WITH liked, collect({id: rec.id, weight: weight, sim: sim})[0..3] AS recGroup
                        UNWIND recGroup AS recData
                        // distinct하게 rec.id들을 모으고, like.weight와 sim(유사도)로 전체를 정렬
                        WITH recData.id AS trackId, recData.weight AS weight, recData.sim AS sim
                        ORDER BY weight DESC, sim DESC
                        // distinct 값을 수집하고, 최종 limit 만큼 잘라서 리턴
                        LIMIT $limit
                        RETURN DISTINCT trackId
            """)
    List<Integer> getRecommendTrackIdsByMemberLikeSim(@Param("memberId") Integer memberId, @Param("tagId") Integer tagId, @Param("limit") Integer limit);

    @Query("""
            MATCH (m:Member {id: $memberId})-[:FOLLOW]->(other:Member)
            WHERE m.enabled = true AND other.enabled = true
                          MATCH (other)-[like:LIKE]->(rec:Track)
                            WHERE rec.enabled = true
                          MATCH (rec)-[:HAVE]->(:Tag {id: $tagId})
                          RETURN DISTINCT rec.id
            """)
    List<Integer> getRecommendTrackIdsByFollowingLike(@Param("memberId") Integer memberId, @Param("tagId") Integer tagId, @Param("limit") Integer limit);

    @Query("""
            MATCH (m:Member {id: $memberId})-[like:LIKE]->(liked:Track)
            WHERE m.enabled = true AND liked.enabled = true
            MATCH (liked)-[similar:SIMILAR]->(rec:Track)-[:HAVE]->(:Tag {id: $tagId})
            WHERE rec.enabled = true
            WITH liked, like, similar, rec
            ORDER BY like.weight DESC, similar.similarity DESC
            // 각 liked 트랙마다 similar 기준 상위 3개만 리스트에 담기
            WITH liked, collect({recId: rec.id, weight: like.weight, sim: similar.similarity})[0..3] AS topRecs
            UNWIND topRecs AS recData
            // 각 rec의 meta 정보(해당 liked의 weight와 유사도)를 유지하며 정렬 (중복 제거 전)
            WITH recData.recId AS trackId, recData.weight AS weight, recData.sim AS sim
            ORDER BY weight DESC, sim DESC
            // 동일 rec.id는 한 번만 선택하고 최종 리스트로 수집
            RETURN DISTINCT trackId
            """)
    List<Integer> getRecommendTrackIdsByFollowingLikeSim(@Param("memberId") Integer memberId, @Param("tagId") Integer tagId, @Param("limit") Integer limit);
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
            LIMIT $limit
            ORDER BY maxWeight DESC
            RETURN t.id AS trackId
            """)
    List<Integer> getMemberFanMix(@Param("memberId") Integer memberId, @Param("limit") Integer limit);
}

