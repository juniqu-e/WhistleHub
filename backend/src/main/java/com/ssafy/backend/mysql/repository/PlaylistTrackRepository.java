package com.ssafy.backend.mysql.repository;

import com.ssafy.backend.mysql.entity.Playlist;
import com.ssafy.backend.mysql.entity.PlaylistTrack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, Integer> {

    void deleteAllByPlaylist(Playlist playlist);

    List<PlaylistTrack> findAllByPlaylist(Playlist playlist);
}
