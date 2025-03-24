package com.ssafy.backend.mysql.repository;

import com.ssafy.backend.mysql.entity.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Integer> {

}
