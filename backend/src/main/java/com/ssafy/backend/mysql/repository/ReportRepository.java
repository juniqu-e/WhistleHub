package com.ssafy.backend.mysql.repository;

import com.ssafy.backend.mysql.entity.Report;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends CrudRepository<Report, Integer> {
}
