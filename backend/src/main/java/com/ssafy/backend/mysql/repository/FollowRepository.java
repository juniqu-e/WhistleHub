package com.ssafy.backend.mysql.repository;

import com.ssafy.backend.mysql.entity.Follow;
import com.ssafy.backend.mysql.entity.Member;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.Optional;
import java.util.List;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Integer> {
    Optional<Follow> findByFromMemberIdAndToMemberId(int followerId, int followingId);
    List<Follow> findByFromMemberId(int memberId, PageRequest pageRequest);
    List<Follow> findByToMemberId(int memberId, PageRequest pageRequest);
    int countByFromMemberId(int memberId);
    int countByToMemberId(int memberId);

   @Query(value = "SELECT f.to_member_id FROM follow f " +
           "JOIN track t ON f.to_member_id = t.member_id " +
           "WHERE f.from_member_id = :memberId " +
           "GROUP BY f.to_member_id " +
           "HAVING COUNT(t.track_id) > 0 " +
           "ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Integer findRandomFollowing(@Param("memberId") int memberId);
}
