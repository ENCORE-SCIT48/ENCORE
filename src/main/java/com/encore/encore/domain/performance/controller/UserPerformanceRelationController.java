// src/main/java/com/encore/encore/domain/performance/controller/UserPerformanceRelationController.java
package com.encore.encore.domain.performance.controller;

import com.encore.encore.domain.performance.dto.PerformanceListItemDto;
import com.encore.encore.domain.performance.service.UserPerformanceRelationService;
import com.encore.encore.global.common.CommonResponse;
import com.encore.encore.global.config.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/performances")
public class UserPerformanceRelationController {

    private final UserPerformanceRelationService userPerformanceRelationService;

    /**
     * 본 공연 여부 판별
     * GET /api/performances/{performanceId}/watched
     * Response: { watched: true/false }
     */
    @GetMapping("/{performanceId}/watched")
    public CommonResponse<Map<String, Object>> isWatched(
        @PathVariable Long performanceId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 현재 SecurityConfig가 permitAll이라 로그인 안 해도 들어올 수 있음 -> null 방어
        if (userDetails == null) {
            return CommonResponse.ok(Map.of("watched", false), "비로그인: 본 공연 여부 false");
        }

        Long userId = userDetails.getUser().getUserId();

        log.info("[UserPerformanceRelation] controller isWatched - userId={}, performanceId={}", userId, performanceId);

        boolean watched = userPerformanceRelationService.isWatched(userId, performanceId);

        return CommonResponse.ok(
            Map.of("watched", watched),
            "본 공연 여부 조회 성공"
        );
    }

    /**
     * 내가 본 공연 목록(본공연리스트 탭용)
     * GET /api/performances/watched?page=0&size=9
     *
     * 엔티티(UserPerformanceRelation) 그대로 반환하면 Hibernate Lazy Proxy 직렬화로 500 터짐
     * PerformanceListItemDto로 내려서 화면에서 바로 렌더링 가능하게 함
     */
    @GetMapping("/watched")
    public CommonResponse<Page<PerformanceListItemDto>> getWatchedPerformances(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "9") int size
    ) {
        if (userDetails == null) {
            return CommonResponse.ok(Page.empty(), "비로그인: 본 공연 목록 없음");
        }

        Long userId = userDetails.getUser().getUserId();
        log.info("[UserPerformanceRelation] controller watched performances - userId={}, page={}, size={}", userId, page, size);

        return CommonResponse.ok(
            userPerformanceRelationService.getWatchedPerformances(userId, PageRequest.of(page, size)),
            "본 공연 목록 조회 성공"
        );
    }
}
