package com.ssafy.backend.mysql.repository;

import com.ssafy.backend.mysql.entity.Follow;
import com.ssafy.backend.mysql.entity.Member;
import com.ssafy.backend.mysql.entity.Track;
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

    @Query(value = "SELECT t.track_id FROM follow f " +
            "JOIN track t ON f.to_member_id = t.member_id " +
            "WHERE f.from_member_id = :memberId " +
            "ORDER BY t.created_at desc "+
            "LIMIT  :size", nativeQuery = true)
    List<Integer> findRecentTrackIdsByFromMemberId(@Param("memberId") int memberId, @Param("size") int size);

    int countByFromMemberId(int memberId);
    int countByToMemberId(int memberId);

}
