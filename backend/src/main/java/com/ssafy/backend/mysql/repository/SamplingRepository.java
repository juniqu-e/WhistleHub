package com.ssafy.backend.mysql.repository;

import com.ssafy.backend.mysql.entity.Sampling;
import com.ssafy.backend.mysql.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SamplingRepository extends JpaRepository<Sampling, Integer> {
    List<Sampling> findAllByTrack(Track track);
    List<Sampling> findAllByOriginTrack(Track track);
}
