package com.ssafy.backend.mysql.repository;

import com.ssafy.backend.mysql.entity.Layer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LayerRepository extends JpaRepository<Layer, Integer> {
}
