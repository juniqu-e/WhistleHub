package com.ssafy.backend.mysql.repository;

import com.ssafy.backend.mysql.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Integer> {
    Optional<Like> findByTrackIdAndMemberId(Integer id, int memberId);
}
