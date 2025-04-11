package com.ssafy.backend.mysql.repository;

import com.ssafy.backend.mysql.entity.Like;
import com.ssafy.backend.mysql.entity.ListenRecord;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListenRecoredRepository extends JpaRepository<ListenRecord, Integer> {
    @Query(value = "SELECT lr.track_id " +
            "FROM listen_record lr " +
            "WHERE lr.member_id = :memberId " +
            "GROUP BY lr.track_id " +
            "ORDER BY MAX(lr.created_at) DESC " +
            "LIMIT :size", nativeQuery = true)
    List<Integer> findByMemberId(int memberId, int size);
}
