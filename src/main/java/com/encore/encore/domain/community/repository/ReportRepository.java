package com.encore.encore.domain.community.repository;

import com.encore.encore.domain.community.entity.Report;
import com.encore.encore.domain.community.entity.ReportTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByReporter_UserIdAndTargetIdAndTargetTypeAndIsDeletedFalse(
        Long userId,
        Long targetId,
        ReportTargetType targetType
    );
}
