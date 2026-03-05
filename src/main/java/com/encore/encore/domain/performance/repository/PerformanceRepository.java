package com.encore.encore.domain.performance.repository;

import com.encore.encore.domain.performance.entity.Performance;
import com.encore.encore.domain.performance.entity.PerformanceCategory;
import com.encore.encore.domain.performance.entity.PerformanceRecruitStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PerformanceRepository extends JpaRepository<Performance, Long> {

    /**
     * 제목 검색 (카테고리 전체일 때 사용)
     *
     * @param title    검색어(공연 제목)
     * @param pageable 페이징 정보
     * @return 검색된 공연 페이지
     */
    Page<Performance> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /**
     * 제목 + 카테고리(category=장르) 검색
     *
     * @param title    검색어(공연 제목)
     * @param category 카테고리(장르)
     * @param pageable 페이징 정보
     * @return 검색된 공연 페이지
     */
    Page<Performance> findByTitleContainingIgnoreCaseAndCategory(String title, PerformanceCategory category, Pageable pageable);

    /**
     * 카테고리(category=장르)만으로 조회
     *
     * @param category 카테고리(장르)
     * @param pageable 페이징 정보
     * @return 카테고리별 공연 페이지
     */
    Page<Performance> findByCategory(PerformanceCategory category, Pageable pageable);

    /**
     * 핫한 공연 Top10 조회 (임시 기준 - recruitStatus=OPEN + createdAt 최신순)
     *
     * @param recruitStatus 모집 상태(예: OPEN)
     * @return 최신순 Top10 공연
     */
    List<Performance> findTop10ByRecruitStatusOrderByCreatedAtDesc(PerformanceRecruitStatus recruitStatus);

    /**
     * 삭제되지 않은 공연 중 최신순 Top 10 (게스트 피드용, status 무관)
     *
     * @return 최신순 공연 목록
     */
    List<Performance> findTop10ByIsDeletedFalseOrderByCreatedAtDesc();

    // 공연 상세 조회: venue를 fetch join 해서 LAZY 문제 방지
    @Query("""
        select p from Performance p
        left join fetch p.venue v
        where p.performanceId = :performanceId
        """)
    Optional<Performance> findDetailById(@Param("performanceId") Long performanceId);

    // 공연의 이름을 가져옴
    @Query("SELECT p.title FROM Performance p WHERE p.performanceId = :performanceId")
    String findTitleByPerformanceId(Long performanceId);

    boolean existsByTitleAndIsDeletedFalse(String title);

    /**
     * 특정 공연장에서 열리는 공연 목록 (공연장 상세·좌석 리뷰용)
     *
     * @param venueId  공연장 ID
     * @param pageable 페이징
     * @return 해당 공연장 공연 페이지
     */
    Page<Performance> findByVenue_VenueIdAndIsDeletedFalseOrderByCreatedAtDesc(
        Long venueId,
        Pageable pageable
    );
}
