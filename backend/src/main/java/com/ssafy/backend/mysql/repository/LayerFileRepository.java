package com.ssafy.backend.mysql.repository;

import com.ssafy.backend.mysql.entity.LayerFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LayerFileRepository extends JpaRepository<LayerFile, Integer> {
}
