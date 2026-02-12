package com.encore.encore.domain.community.repository;

import com.encore.encore.domain.community.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 최신순 정렬
     * - 특정 공연의 공연 후기만 조회 (seat IS NULL)
     * - 작성자(user)까지 fetch (N+1 방지)
     * - createdAt DESC (작성일 최신순)
     */
    @EntityGraph(attributePaths = {"user"})
    Page<Review> findByPerformance_PerformanceIdAndSeatIsNullOrderByCreatedAtDesc(
        Long performanceId,
        Pageable pageable
    );

    /**
     * 별점순 정렬 (동점이면 최신순)
     * - rating DESC, createdAt DESC
     */
    @EntityGraph(attributePaths = {"user"})
    Page<Review> findByPerformance_PerformanceIdAndSeatIsNullOrderByRatingDescCreatedAtDesc(
        Long performanceId,
        Pageable pageable
    );

    // 공연 후기(좌석후기 제외) 전체 평균/개수 집계
    @Query("""
    select
        coalesce(avg(r.rating), 0),
        count(r.reviewId)
    from Review r
    where r.performance.performanceId = :performanceId
      and r.seat is null
    """)
    Object[] getPerformanceReviewSummary(@Param("performanceId") Long performanceId);
}
