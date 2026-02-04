package com.encore.encore.domain.performance.repository;

import com.encore.encore.domain.performance.entity.Performance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PerformanceRepository extends JpaRepository<Performance, Long> {

    /**
     * 제목 검색 (카테고리 전체일 때 사용)
     * @param title 검색어(공연 제목)
     * @param pageable 페이징 정보
     * @return 검색된 공연 페이지
     */
    Page<Performance> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /**
     * 제목 + 카테고리(status) 검색
     * @param title 검색어(공연 제목)
     * @param status 카테고리
     * @param pageable 페이징 정보
     * @return 검색된 공연 페이지
     */
    Page<Performance> findByTitleContainingIgnoreCaseAndStatus(String title, String status, Pageable pageable);

    /**
     * 카테고리(status)만으로 조회
     * @param status 카테고리
     * @param pageable 페이징 정보
     * @return 카테고리별 공연 페이지
     */
    Page<Performance> findByStatus(String status, Pageable pageable);

    /**
     * 핫한 공연 Top10 조회 (임시 기준 - createdAt 최신순)
     * @param status 대상 상태(예: OPEN)
     * @return 최신순 Top10 공연
     */
    List<Performance> findTop10ByStatusOrderByCreatedAtDesc(String status);
}
