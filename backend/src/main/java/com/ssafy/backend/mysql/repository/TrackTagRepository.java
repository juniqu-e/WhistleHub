package com.ssafy.backend.mysql.repository;

import com.ssafy.backend.mysql.entity.TrackTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackTagRepository extends JpaRepository<TrackTag, Integer> {
}
