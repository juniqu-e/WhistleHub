package com.ssafy.backend.mysql.repository;

import com.ssafy.backend.mysql.entity.Track;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackRepository extends JpaRepository<Track, Integer> {
    List<Track> findAllByTitleContains(String title, PageRequest pageRequest);

    List<Track> findByMemberIdAndVisibility(int memberId, boolean visibility, PageRequest pageRequest);
    List<Track> findByMemberId(int memberId, PageRequest pageRequest);
}
