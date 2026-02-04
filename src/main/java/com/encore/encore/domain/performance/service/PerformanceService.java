package com.encore.encore.domain.performance.service;

import com.encore.encore.domain.performance.dto.PerformanceDetailDto;
import com.encore.encore.domain.performance.dto.PerformanceListItemDto;
import com.encore.encore.domain.performance.entity.Performance;
import com.encore.encore.domain.performance.repository.PerformanceRepository;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceService {

    private final PerformanceRepository performanceRepository;

    /**
     * 공연 목록 조회 (검색/카테고리/페이징 지원)
     *
     * - category가 "전체" 또는 비어 있으면 - 제목 검색만 적용하거나(검색어 있을 때), 전체 조회
     * - category가 특정 값이면 status 필터를 적용하고, 검색어가 있으면 title도 함께 검색
     *
     * @param keyword 공연 제목 검색어(null/빈값 가능)
     * @param category 카테고리(전체/밴드/뮤지컬/연극 등, null/빈값 가능)
     * @param pageable 페이징 정보
     * @return 공연 목록 페이지(리스트 DTO)
     */
    public Page<PerformanceListItemDto> getPerformances(String keyword, String category, Pageable pageable) {

        boolean hasKeyword = StringUtils.hasText(keyword);
        boolean hasCategory = StringUtils.hasText(category) && !"전체".equals(category);

        log.info("[Performance] list request - keyword={}, category={}, page={}, size={}",
            keyword,
            category,
            pageable.getPageNumber(),
            pageable.getPageSize()
        );

        Page<Performance> performances;

        // 경우의 수를 명확히 나눠야 "전체 + 검색" 같은 케이스에서 검색이 무시되는 버그를 막을 수 있음
        if (hasCategory && hasKeyword) {
            // 카테고리 + 검색어
            performances = performanceRepository.findByTitleContainingIgnoreCaseAndStatus(keyword, category, pageable);
        } else if (hasCategory) {
            // 카테고리만
            performances = performanceRepository.findByStatus(category, pageable);
        } else if (hasKeyword) {
            // 전체 + 검색어
            performances = performanceRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        } else {
            // 전체
            performances = performanceRepository.findAll(pageable);
        }

        log.info("[Performance] list result - totalElements={}, totalPages={}",
            performances.getTotalElements(),
            performances.getTotalPages()
        );

        return performances.map(PerformanceListItemDto::new);
    }

    /**
     * 공연 상세 정보를 조회. 대상이 없으면 NOT_FOUND 예외를 발생.
     * @param performanceId 공연 ID
     * @return 공연 상세 DTO
     */
    public PerformanceDetailDto getPerformance(Long performanceId) {
        log.info("[Performance] detail request - performanceId={}", performanceId);

        Performance performance = performanceRepository.findById(performanceId)
            .orElseThrow(() -> new ApiException(
                ErrorCode.NOT_FOUND,
                "공연을 찾을 수 없습니다. performanceId=" + performanceId
            ));

        log.info("[Performance] detail found - performanceId={}", performanceId);
        return new PerformanceDetailDto(performance);
    }

    /**
     * 핫한 공연 Top10을 조회. (임시 기준 - OPEN 상태 + createdAt 최신순)
     * @return 핫한 공연 리스트(리스트 DTO)
     */
    public List<PerformanceListItemDto> getHotPerformances() {
        log.info("[Performance] hot list request - status=OPEN, limit=10");

        List<PerformanceListItemDto> result = performanceRepository
            .findTop10ByStatusOrderByCreatedAtDesc("OPEN")
            .stream()
            .map(PerformanceListItemDto::new)
            .toList();

        log.info("[Performance] hot list result - size={}", result.size());
        return result;
    }
}
