package com.ssafy.backend.graph.repository;

import com.ssafy.backend.graph.model.entity.MemberNode;
import org.neo4j.driver.internal.value.PathValue;
import org.neo4j.driver.types.Path;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface MemberNodeRepository extends Neo4jRepository<MemberNode, Integer> {

    @Query("MATCH (m:Member), (t:Track) " +
            "WHERE m.id = $memberId AND t.id = $trackId " +
            "MERGE (m)-[r:LIKE]->(t) " +
            "ON CREATE SET r.weight = $weight " +
            "ON MATCH SET r.weight = r.weight + $weight")
    void createOrIncreaseLikeRelationship(@Param("memberId") Integer memberId,
                                          @Param("trackId") Integer trackId,
                                          @Param("weight") Double weight);

    @Query("MATCH (m:Member), (t:Tag) " +
            "WHERE m.id = $memberId AND t.id = $tagId " +
            "MERGE (m)-[r:PREFER]->(t) " +
            "ON CREATE SET r.weight = $weight " +
            "ON MATCH SET r.weight = r.weight + $weight")
    void createOrIncreasePreferRelationship(@Param("memberId") Integer memberId,
                                            @Param("tagId") Integer tagId,
                                            @Param("weight") Double weight);

    @Query("MATCH (m:Member)-[r:LIKE]->(t:Track) " +
            "WHERE m.id = $memberId AND t.id = $trackId " +
            "RETURN COUNT(r) > 0")
    boolean existsLikeRelationship(@Param("memberId") Integer memberId,
                                   @Param("trackId") Integer trackId);

    @Query("MATCH (m:Member)-[r:PREFER]->(t:Tag) " +
            "WHERE m.id = $memberId AND t.id = $tagId " +
            "RETURN COUNT(r) > 0")
    boolean existsPreferRelationship(@Param("memberId") Integer memberId,
                                     @Param("tagId") Integer tagId);

    @Query("MATCH (m:Member), (t:Track) " +
            "WHERE m.id = $memberId AND t.id = $trackId " +
            "MERGE (m)-[r:LIKE]->(t) " +
            "ON CREATE SET r.weight = $weight")
    void createLikeRelationship(@Param("memberId") Integer memberId,
                                @Param("trackId") Integer trackId,
                                @Param("weight") Double weight);

    @Query("MATCH (m:Member), (t:Tag) " +
            "WHERE m.id = $memberId AND t.id = $tagId " +
            "MERGE (m)-[r:PREFER]->(t) " +
            "ON CREATE SET r.weight = $weight")
    void createPreferRelationship(@Param("memberId") Integer memberId,
                                  @Param("tagId") Integer tagId,
                                  @Param("weight") Double weight);

    @Query("MATCH (m:Member), (t:Track) " +
            "WHERE m.id = $memberId AND t.id = $trackId " +
            "MERGE (m)-[r:LIKE]->(t) " +
            "ON MATCH SET r.weight = $newWeight")
    void updateLikeWeight(@Param("memberId") Integer memberId,
                          @Param("trackId") Integer trackId,
                          @Param("newWeight") Double newWeight);

    @Query("MATCH (m:Member), (t:Tag) " +
            "WHERE m.id = $memberId AND t.id = $tagId " +
            "MERGE (m)-[r:PREFER]->(t) " +
            "ON MATCH SET r.weight = $newWeight")
    void updatePreferWeight(@Param("memberId") Integer memberId,
                            @Param("tagId") Integer tagId,
                            @Param("newWeight") Double newWeight);

    @Query("MATCH (m:Member), (t:Track) " +
            "WHERE m.id = $memberId AND t.id = $trackId " +
            "MERGE (m)-[r:LIKE]->(t) " +
            "ON MATCH SET r.weight = r.weight + $increment")
    void increaseLikeWeight(@Param("memberId") Integer memberId,
                            @Param("trackId") Integer trackId,
                            @Param("increment") Double increment);

    @Query("MATCH (m:Member), (t:Tag) " +
            "WHERE m.id = $memberId AND t.id = $tagId " +
            "MERGE (m)-[r:PREFER]->(t) " +
            "ON MATCH SET r.weight = r.weight + $increment")
    void increasePreferWeight(@Param("memberId") Integer memberId,
                              @Param("tagId") Integer tagId,
                              @Param("increment") Double increment);

    @Query("MATCH (m1:Member), (m2:Member) " +
            "WHERE m1.id = $followerId AND m2.id = $followingId " +
            "MERGE (m1)-[:FOLLOW]->(m2)")
    void createFollowRelationship(@Param("followerId") Integer followerId,
                                  @Param("followingId") Integer followingId);

    @Query("MATCH(m1:Member)-[r:FOLLOW]->(m2:Member) "  +
            "WHERE m1.id = $followerId AND m2.id = $followingId " +
            "DELETE r")
    void deleteFollowRelationship(@Param("followerId") Integer followerId,
                                  @Param("followingId") Integer followingId);

    @Query("MATCH (:Member {id: $memberId})-[:FOLLOW]->(f:Member) " +
            "WITH f " +
            "MATCH (m:Member)-[:FOLLOW]->(f) " +
            "WHERE size([(m)-[:LIKE]->(:Track) | 1]) >= $size" +
            "ORDER BY rand() " +
            "RETURN f.id")
    Integer getFanmixMember(@Param("memberId") Integer memberId, @Param("size") Integer size);
}
