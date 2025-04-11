package com.ssafy.backend.mysql.repository;

import com.ssafy.backend.mysql.entity.Track;
import com.ssafy.backend.mysql.entity.TrackTag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackTagRepository extends JpaRepository<TrackTag, Integer> {
    List<TrackTag> findAllByTrack(Track track);
    List<TrackTag> findByTagId(int tagId, PageRequest pageRequest);
}
