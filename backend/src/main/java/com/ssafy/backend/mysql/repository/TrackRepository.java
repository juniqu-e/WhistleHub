package com.ssafy.backend.mysql.repository;

import com.ssafy.backend.mysql.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackRepository extends JpaRepository<Track, Integer> {
}
