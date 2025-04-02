package com.ssafy.backend.mysql.repository;

import com.ssafy.backend.mysql.entity.Follow;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;
import java.util.List;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Integer> {
    Optional<Follow> findByFromMemberIdAndToMemberId(int followerId, int followingId);
    List<Follow> findByFromMemberId(int memberId, PageRequest pageRequest);
    List<Follow> findByToMemberId(int memberId, PageRequest pageRequest);

}
