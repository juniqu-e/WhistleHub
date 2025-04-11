package com.ssafy.backend.mysql.repository;

import com.ssafy.backend.mysql.entity.Playlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Integer> {

    List<Playlist> findAllByMemberId(int memberId, PageRequest pageRequest);
}
