package com.encore.encore.domain.member.repository;

import com.encore.encore.domain.performance.entity.UserPerformanceRelation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPerformanceRelationInfoRepository extends JpaRepository<UserPerformanceRelation, Long> {

    List<UserPerformanceRelation> findTop5ByUser_UserIdOrderByCreatedAtDesc(Long loginUserId);
}
