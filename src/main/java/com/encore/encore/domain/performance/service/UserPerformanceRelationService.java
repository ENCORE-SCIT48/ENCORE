// src/main/java/com/encore/encore/domain/performance/service/UserPerformanceRelationService.java
package com.encore.encore.domain.performance.service;

import com.encore.encore.domain.performance.dto.PerformanceListItemDto;
import com.encore.encore.domain.performance.entity.Performance;
import com.encore.encore.domain.performance.repository.UserPerformanceRelationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPerformanceRelationService {

    private static final String WATCHED = "WATCHED";

    private final UserPerformanceRelationRepository userPerformanceRelationRepository;

    /**
     * 본 공연 여부 판별
     * @param userId 로그인 유저 ID
     * @param performanceId 공연 ID
     * @return 본 공연이면 true
     */
    public boolean isWatched(Long userId, Long performanceId) {
        log.info("[UserPerformanceRelation] isWatched request - userId={}, performanceId={}", userId, performanceId);

        boolean result = userPerformanceRelationRepository
            .existsByUser_UserIdAndPerformance_PerformanceIdAndStatusAndIsDeletedFalse(userId, performanceId, WATCHED);

        log.info("[UserPerformanceRelation] isWatched result - {}", result);
        return result;
    }

    /**
     * 내가 본 공연 목록 조회 (WATCHED)
     * - 엔티티(UserPerformanceRelation) 반환 금지(직렬화 500 방지)
     * - Performance를 조회해서 PerformanceListItemDto로 변환해 반환
     */
    public Page<PerformanceListItemDto> getWatchedPerformances(Long userId, Pageable pageable) {
        log.info("[UserPerformanceRelation] watched performances request - userId={}, page={}, size={}",
            userId, pageable.getPageNumber(), pageable.getPageSize()
        );

        Page<Performance> page = userPerformanceRelationRepository
            .findPerformancesByUserIdAndStatusOrderByWatchedAtDesc(userId, WATCHED, pageable);

        log.info("[UserPerformanceRelation] watched performances result - totalElements={}, totalPages={}",
            page.getTotalElements(), page.getTotalPages()
        );

        return page.map(PerformanceListItemDto::new);
    }
}
