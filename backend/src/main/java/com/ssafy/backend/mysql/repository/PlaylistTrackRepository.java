package com.ssafy.backend.mysql.repository;

import com.ssafy.backend.mysql.entity.PlaylistTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, Integer> {

}
