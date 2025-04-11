package com.ssafy.backend.mysql.repository;

import com.ssafy.backend.mysql.entity.Like;
import com.ssafy.backend.playlist.dto.TrackInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface LikeRepository extends JpaRepository<Like, Integer> {
    Optional<Like> findByTrackIdAndMemberId(Integer id, int memberId);

    List<Like> findByMemberId(int memberId, PageRequest pageRequest);


    @Query("SELECT l.track.id as trackId " +
            "FROM Like l " +
            "JOIN TrackTag tt ON l.track.id = tt.track.id " +
            "WHERE tt.id.tagId = :tagId " +
            "AND l.createdAt >= :startDate " +
            "GROUP BY l.track.id " +
            "ORDER BY COUNT(l) DESC " +
            "LIMIT :limit")
    List<Integer> findLikeRankingByTagAndPeriod(@Param("tagId") Integer tagId, @Param("startDate") String startDate, @Param("limit") Integer limit);


}
