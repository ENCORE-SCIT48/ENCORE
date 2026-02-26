package com.encore.encore.domain.community.repository;

import com.encore.encore.domain.community.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
