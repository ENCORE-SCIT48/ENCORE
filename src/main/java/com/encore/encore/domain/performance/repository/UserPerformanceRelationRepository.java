// src/main/java/com/encore/encore/domain/performance/repository/UserPerformanceRelationRepository.java
package com.encore.encore.domain.performance.repository;

import com.encore.encore.domain.performance.entity.Performance;
import com.encore.encore.domain.performance.entity.UserPerformanceRelation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserPerformanceRelationRepository extends JpaRepository<UserPerformanceRelation, Long> {

    boolean existsByUser_UserIdAndPerformance_PerformanceIdAndStatusAndIsDeletedFalse(
        Long userId,
        Long performanceId,
        String status
    );

    /**
     * 본 공연(WATCHED) 목록을 "Performance"로 바로 조회
     * - 엔티티(UserPerformanceRelation)를 반환하면 Lazy Proxy 직렬화로 500 터질 수 있어서
     *   Performance를 바로 반환 -> Service에서 DTO로 변환하는 방식 사용
     */
    @Query("""
        select upr.performance
        from UserPerformanceRelation upr
        where upr.user.userId = :userId
          and upr.status = :status
          and upr.isDeleted = false
        order by upr.watchedAt desc
    """)
    Page<Performance> findPerformancesByUserIdAndStatusOrderByWatchedAtDesc(
        @Param("userId") Long userId,
        @Param("status") String status,
        Pageable pageable
    );
}
