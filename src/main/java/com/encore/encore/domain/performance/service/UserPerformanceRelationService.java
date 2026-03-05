package com.encore.encore.domain.performance.service;

import com.encore.encore.domain.community.entity.ReportTargetType;
import com.encore.encore.domain.community.repository.ReportRepository;
import com.encore.encore.domain.performance.dto.PerformanceListItemDto;
import com.encore.encore.domain.performance.entity.Performance;
import com.encore.encore.domain.performance.entity.UserPerformanceRelation;
import com.encore.encore.domain.performance.repository.PerformanceRepository;
import com.encore.encore.domain.performance.repository.UserPerformanceRelationRepository;
import com.encore.encore.domain.user.entity.User;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPerformanceRelationService {

    private static final String WATCHED = "WATCHED";
    private static final String WISHED = "WISHED";

    private final UserPerformanceRelationRepository userPerformanceRelationRepository;
    private final PerformanceRepository performanceRepository;
    private final ReportRepository reportRepository;

    public boolean isWatched(Long userId, Long performanceId) {
        log.info("[UserPerformanceRelation] isWatched request - userId={}, performanceId={}", userId, performanceId);

        boolean result = userPerformanceRelationRepository
            .existsByUser_UserIdAndPerformance_PerformanceIdAndStatusAndIsDeletedFalse(userId, performanceId, WATCHED);

        log.info("[UserPerformanceRelation] isWatched result - {}", result);
        return result;
    }

    @Transactional
    public boolean toggleWatched(Long userId, Long performanceId) {

        log.info("[UserPerformanceRelation] toggleWatched request - userId={}, performanceId={}", userId, performanceId);

        // 공연 존재 검증
        if (!performanceRepository.existsById(performanceId)) {
            throw new ApiException(ErrorCode.NOT_FOUND, "공연을 찾을 수 없습니다. performanceId=" + performanceId);
        }

        // 현재 본공연 상태면 -> 최신 살아있는 row 논리삭제
        return userPerformanceRelationRepository
            .findTopByUser_UserIdAndPerformance_PerformanceIdAndStatusAndIsDeletedFalseOrderByRelationIdDesc(
                userId, performanceId, WATCHED
            )
            .map(upr -> {
                upr.delete(); // is_deleted=true
                return false; // 최종 상태: 본공연 해제
            })
            .orElseGet(() -> {
                // 본공연 아니면 -> 새 row INSERT
                UserPerformanceRelation created = UserPerformanceRelation.builder()
                    .user(User.builder().userId(userId).build())
                    .performance(Performance.builder().performanceId(performanceId).build())
                    .status(WATCHED)
                    .watchedAt(java.time.LocalDateTime.now())
                    .build();

                userPerformanceRelationRepository.save(created);
                return true; // 최종 상태: 본공연 됨
            });
    }

    @Transactional(readOnly = true)
    public boolean isWished(Long userId, Long performanceId) {
        return userPerformanceRelationRepository
            .existsByUser_UserIdAndPerformance_PerformanceIdAndStatusAndIsDeletedFalse(
                userId, performanceId, WISHED
            );
    }

    public Page<PerformanceListItemDto> getWatchedPerformances(Long userId, Pageable pageable, String keyword) {
        log.info("[UserPerformanceRelation] watched performances request - userId={}, page={}, size={}, keyword={}",
            userId, pageable.getPageNumber(), pageable.getPageSize(), keyword
        );

        Page<Performance> page = userPerformanceRelationRepository
            .findPerformancesByUserIdAndStatusAndKeywordOrderByWatchedAtDesc(userId, WATCHED, keyword, pageable);

        return page.map(PerformanceListItemDto::new);
    }

    // 찜 토글
    @Transactional
    public boolean toggleWish(Long userId, Long performanceId) {

        log.info("[UserPerformanceRelation] toggleWish request - userId={}, performanceId={}", userId, performanceId);

        // 공연 존재 검증
        if (!performanceRepository.existsById(performanceId)) {
            throw new ApiException(ErrorCode.NOT_FOUND, "공연을 찾을 수 없습니다. performanceId=" + performanceId);
        }

        // 현재 찜 상태면 -> 최신 살아있는 row를 논리삭제
        return userPerformanceRelationRepository
            .findTopByUser_UserIdAndPerformance_PerformanceIdAndStatusAndIsDeletedFalseOrderByRelationIdDesc(
                userId, performanceId, WISHED
            )
            .map(upr -> {
                upr.delete(); // is_deleted = true (논리삭제)
                return false; // 최종 상태: 찜 해제
            })
            .orElseGet(() -> {
                // 찜 상태 아니면 -> 새 row INSERT
                UserPerformanceRelation created = UserPerformanceRelation.builder()
                    .user(User.builder().userId(userId).build())
                    .performance(Performance.builder().performanceId(performanceId).build())
                    .status(WISHED)
                    .watchedAt(null)
                    .build();

                userPerformanceRelationRepository.save(created);
                return true; // 최종 상태: 찜 됨
            });
    }

    public Page<PerformanceListItemDto> getWishedPerformances(Long userId, Pageable pageable, String keyword) {
        log.info("[UserPerformanceRelation] wished performances request - userId={}, page={}, size={}, keyword={}",
            userId, pageable.getPageNumber(), pageable.getPageSize(), keyword
        );

        Page<Performance> page = userPerformanceRelationRepository
            .findPerformancesByUserIdAndStatusAndKeywordOrderByCreatedAtDesc(userId, WISHED, keyword, pageable);

        return page.map(PerformanceListItemDto::new);
    }

    @Transactional(readOnly = true)
    public boolean isReported(Long userId, Long performanceId) {
        return reportRepository.existsByReporter_UserIdAndTargetIdAndTargetTypeAndIsDeletedFalse(
            userId,
            performanceId,
            ReportTargetType.PERFORMANCE
        );
    }
}
