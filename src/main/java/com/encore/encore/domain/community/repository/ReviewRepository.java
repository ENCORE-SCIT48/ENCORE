package com.encore.encore.domain.community.repository;

import com.encore.encore.domain.community.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 특정 공연에 대한 공연 후기 목록 조회
     * - 좌석 후기(seat_id가 존재하는 리뷰)는 제외
     * - 작성자 정보(user)를 함께 조회하기 위해 EntityGraph를 사용(N+1 쿼리 방지 목적)
     * - 페이징 처리를 지원
     *
     * @param performanceId 공연 ID
     * @param pageable 페이징 정보
     * @return 공연 후기 페이지
     */
    @EntityGraph(attributePaths = {"user"})
    Page<Review> findByPerformance_PerformanceIdAndSeatIsNull(
        Long performanceId,
        Pageable pageable
    );
}
