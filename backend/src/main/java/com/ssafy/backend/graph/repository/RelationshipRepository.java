package com.ssafy.backend.graph.repository;

import com.ssafy.backend.graph.model.entity.TagNode;
import com.ssafy.backend.graph.model.entity.TrackNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelationshipRepository extends Neo4jRepository<TrackNode, Integer> {

    @Query("MATCH (t1:Track {id: $trackId1}), (t2:Track {id: $trackId2}) " +
            "MERGE (t1)-[:SIMILAR{similarity: $similarity}]->(t2)")
    void createSimilarRelationship(@Param("trackId1") Integer trackId1,
                                   @Param("trackId2") Integer trackId2,
                                      @Param("similarity") Double similarity);

    @Query("MATCH (t:Track {id: $trackId}), (tag:Tag {id: $tagId}) " +
            "MERGE (t)-[:HAVE]->(tag)")
    void createHaveRelationship(@Param("trackId") Integer trackId,
                                @Param("tagId") Integer tagId);

    @Query("MATCH (m:Member {id: $memberId}), (t:Track {id: $trackId}) " +
            "MERGE (m)-[:WRITE]->(t)")
    void createWriteRelationship(@Param("memberId") Integer memberId,
                                 @Param("trackId") Integer trackId);

    @Query("MATCH (m:Member{id: $memberId})-[p:PREFER]->(t:Tag) order by p.weight desc return t")
    List<TagNode> findPreferTagsByMemberId(@Param("memberId") Integer memberId);
}
