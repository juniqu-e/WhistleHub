package com.ssafy.backend.graph.repository;

import com.ssafy.backend.graph.model.entity.MemberNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * <pre>Member 노드 repo</pre>
 *
 * @author 박병주
 * @version 1.0
 * @since 2025-03-12
 */
public interface MemberNodeRepository extends Neo4jRepository<MemberNode, Integer> {
    // member 기점으로 3 depth cypher query
    // 또는 파라미터를 받는 버전
    @Query("MATCH (u:User {name: $memberName})-[r*..3]-(n) RETURN DISTINCT u, r, n")
    List<MemberNode> findUserNetworkByName(@Param("memberName") String memberName);

//    @Query("MERGE (m:Member {memberId: $memberId}) RETURN m")
//    MemberNode upsertMember(int memberId);

    Optional<MemberNode> findByMemberId(@Param("memberId") int memberId);
}