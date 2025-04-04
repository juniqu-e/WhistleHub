package com.ssafy.backend.mysql.repository;

import com.ssafy.backend.mysql.entity.Like;
import com.ssafy.backend.mysql.entity.ListenRecord;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListenRecoredRepository extends JpaRepository<ListenRecord, Integer> {
    List<ListenRecord> findByMemberId(int memberId, PageRequest pageRequest);
}
