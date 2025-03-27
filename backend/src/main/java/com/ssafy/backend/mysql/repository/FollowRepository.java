package com.ssafy.backend.mysql.repository;

import com.ssafy.backend.mysql.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Integer> {
    Optional<Follow> findByFromMemberIdAndToMemberId(int followerId, int followingId);
    void deleteByFromMemberIdAndToMemberId(int followerId, int followingId);
}
