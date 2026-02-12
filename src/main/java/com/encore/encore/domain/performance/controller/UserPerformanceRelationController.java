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
     * 내가 본 공연 목록 조회 (본공연리스트 탭용)
     * GET /api/performances/watched?page=0&size=9&keyword=검색어
     *
     * - 로그인한 사용자의 WATCHED 상태 공연만 조회
     * - keyword가 존재하면 공연 제목 기준으로 검색
     * - 엔티티(UserPerformanceRelation)를 그대로 반환하면 Hibernate Lazy Proxy 직렬화로 500 에러가 발생할 수 있으므로
     *   PerformanceListItemDto로 변환하여 반환
     *
     * @param userDetails 로그인 사용자 정보
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 당 개수
     * @param keyword 공연 제목 검색어 (선택)
     * @return 본 공연 목록 페이지
     */
    @GetMapping("/watched")
    public CommonResponse<Page<PerformanceListItemDto>> getWatchedPerformances(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "9") int size,
        @RequestParam(required = false) String keyword
    ) {
        if (userDetails == null) {
            return CommonResponse.ok(Page.empty(), "비로그인: 본 공연 목록 없음");
        }

        Long userId = userDetails.getUser().getUserId();
        log.info("[UserPerformanceRelation] controller watched performances - userId={}, page={}, size={}, keyword={}",
            userId, page, size, keyword
        );

        return CommonResponse.ok(
            userPerformanceRelationService.getWatchedPerformances(userId, PageRequest.of(page, size), keyword),
            "본 공연 목록 조회 성공"
        );
    }

    // 찜 여부 조회
    @GetMapping("/{performanceId}/wish")
    public CommonResponse<Map<String, Object>> isWished(
        @PathVariable Long performanceId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return CommonResponse.ok(Map.of("wished", false), "비로그인: 찜 여부 false");
        }

        Long userId = userDetails.getUser().getUserId();
        boolean wished = userPerformanceRelationService.isWished(userId, performanceId);

        return CommonResponse.ok(Map.of("wished", wished), "찜 여부 조회 성공");
    }

    // 찜 토글
    @PostMapping("/{performanceId}/wish")
    public CommonResponse<Map<String, Object>> toggleWish(
        @PathVariable Long performanceId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return CommonResponse.ok(Map.of("wished", false), "비로그인: 찜 불가");
        }

        Long userId = userDetails.getUser().getUserId();
        boolean wished = userPerformanceRelationService.toggleWish(userId, performanceId);

        return CommonResponse.ok(Map.of("wished", wished), "찜 토글 성공");
    }

    @GetMapping("/wished")
    public CommonResponse<Page<PerformanceListItemDto>> getWishedPerformances(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "9") int size,
        @RequestParam(required = false) String keyword
    ) {
        if (userDetails == null) {
            return CommonResponse.ok(Page.empty(), "비로그인: 북마크 목록 없음");
        }

        Long userId = userDetails.getUser().getUserId();
        log.info("[UserPerformanceRelation] controller wished performances - userId={}, page={}, size={}, keyword={}",
            userId, page, size, keyword
        );

        return CommonResponse.ok(
            userPerformanceRelationService.getWishedPerformances(userId, PageRequest.of(page, size), keyword),
            "북마크(찜) 공연 목록 조회 성공"
        );
    }
}
