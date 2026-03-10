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
     * - 삭제된 리뷰 제외 (isDeleted = false)
     * - 작성자(user)까지 fetch (N+1 방지)
     * - createdAt DESC (작성일 최신순)
     */
    @EntityGraph(attributePaths = {"user"})
    Page<Review> findByPerformance_PerformanceIdAndSeatIsNullAndIsDeletedFalseOrderByCreatedAtDesc(
        Long performanceId,
        Pageable pageable
    );

    /**
     * 별점순 정렬 (동점이면 최신순)
     * - 삭제된 리뷰 제외 (isDeleted = false)
     * - rating DESC, createdAt DESC
     */
    @EntityGraph(attributePaths = {"user"})
    Page<Review> findByPerformance_PerformanceIdAndSeatIsNullAndIsDeletedFalseOrderByRatingDescCreatedAtDesc(
        Long performanceId,
        Pageable pageable
    );

    // 공연 후기(좌석후기 제외) 전체 평균/개수 집계 (삭제된 리뷰 제외)
    @Query("""
        select
            coalesce(avg(r.rating), 0),
            count(r.reviewId)
        from Review r
        where r.performance.performanceId = :performanceId
          and r.seat is null
          and r.isDeleted = false
        """)
    Object[] getPerformanceReviewSummary(@Param("performanceId") Long performanceId);

    /**
     * [설명] 특정 공연의 좌석 리뷰만 조회 (seat IS NOT NULL, 삭제 제외, 최신순)
     *
     * @param performanceId 공연 ID
     * @param pageable      페이징
     * @return 좌석 리뷰 페이지
     */
    @EntityGraph(attributePaths = {"user", "seat"})
    Page<Review> findByPerformance_PerformanceIdAndSeatIsNotNullAndIsDeletedFalseOrderByCreatedAtDesc(
        Long performanceId,
        Pageable pageable
    );

    /**
     * [설명] 좌석 리뷰 요약 (평균 별점, 개수) - 해당 공연의 좌석 리뷰만 집계
     *
     * @param performanceId 공연 ID
     * @return [0]=avgRating, [1]=reviewCount
     */
    @Query("""
        select coalesce(avg(r.rating), 0), count(r.reviewId)
        from Review r
        where r.performance.performanceId = :performanceId
          and r.seat is not null
          and r.isDeleted = false
        """)
    Object[] getSeatReviewSummary(@Param("performanceId") Long performanceId);

    /**
     * [피드] 최근 공연 후기(좌석 리뷰 제외) 조회
     * - seat IS NULL, 리뷰/공연 모두 isDeleted = false
     * - createdAt DESC
     */
    @EntityGraph(attributePaths = {"performance", "user"})
    Page<Review> findBySeatIsNullAndIsDeletedFalseAndPerformance_IsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    /**
     * [피드] 최근 좌석 리뷰 조회
     * - seat IS NOT NULL, 리뷰/공연 모두 isDeleted = false
     * - createdAt DESC
     */
    @EntityGraph(attributePaths = {"performance", "user", "seat"})
    Page<Review> findBySeatIsNotNullAndIsDeletedFalseAndPerformance_IsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    /**
     * [피드] 사용자가 이미 공연 후기를 남긴 performanceId 목록 (좌석 리뷰 제외)
     */
    @Query("""
        select distinct r.performance.performanceId
        from Review r
        where r.user.userId = :userId
          and r.seat is null
          and r.isDeleted = false
        """)
    java.util.List<Long> findReviewedPerformanceIdsByUser(@Param("userId") Long userId);
}
