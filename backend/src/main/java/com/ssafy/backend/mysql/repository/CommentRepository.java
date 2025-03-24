package com.ssafy.backend.mysql.repository;

import com.ssafy.backend.mysql.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    Optional<Comment> findByTrackIdAndMemberId(int trackId, int memberId);

    List<Comment> findAllByTrackId(int trackId);
}
