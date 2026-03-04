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
     * 본 공연 여부 조회
     *
     * 현재 SecurityConfig가 permitAll 설정이라
     * 비로그인 상태에서도 호출될 수 있으므로 null 방어 로직을 포함
     *
     * @param performanceId 공연 ID
     * @param userDetails 로그인 사용자 정보
     * @return 본 공연 여부(watched: true/false)
     */
    @GetMapping("/{performanceId}/watched")
    public CommonResponse<Map<String, Object>> isWatched(
        @PathVariable Long performanceId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
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
     * 내가 본 공연 목록 조회
     *
     * 로그인한 사용자의 WATCHED 상태 공연만 조회
     * keyword가 존재하면 공연 제목 기준으로 검색
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

    /**
     * 찜 여부 조회
     *
     * @param performanceId 공연 ID
     * @param userDetails 로그인 사용자 정보
     * @return 찜 여부(wished: true/false)
     */
    @GetMapping("/{performanceId}/wish")
    public CommonResponse<Map<String, Object>> isWished(
        @PathVariable Long performanceId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return CommonResponse.ok(Map.of("wished", false), "비로그인: 찜 여부 false");
        }

        Long userId = userDetails.getUser().getUserId();

        log.info("[UserPerformanceRelation] controller isWished - userId={}, performanceId={}",
            userId, performanceId
        );

        boolean wished = userPerformanceRelationService.isWished(userId, performanceId);

        return CommonResponse.ok(Map.of("wished", wished), "찜 여부 조회 성공");
    }

    /**
     * 본 공연 상태 토글
     *
     * WATCHED 상태가 존재하면 논리 삭제하고,
     * 존재하지 않으면 새로 생성
     *
     * @param performanceId 공연 ID
     * @param userDetails 로그인 사용자 정보
     * @return 최종 본 공연 상태(watched: true/false)
     */
    @PostMapping("/{performanceId}/watched")
    public CommonResponse<Map<String, Object>> toggleWatched(
        @PathVariable Long performanceId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return CommonResponse.ok(Map.of("watched", false), "비로그인: 본공연 체크 불가");
        }

        Long userId = userDetails.getUser().getUserId();

        log.info("[UserPerformanceRelation] controller toggleWatched - userId={}, performanceId={}",
            userId, performanceId
        );

        boolean watched = userPerformanceRelationService.toggleWatched(userId, performanceId);

        return CommonResponse.ok(Map.of("watched", watched), "본공연 토글 성공");
    }

    /**
     * 찜 상태를 토글
     *
     * WISHED 상태가 존재하면 논리 삭제하고,
     * 존재하지 않으면 새로 생성
     *
     * @param performanceId 공연 ID
     * @param userDetails 로그인 사용자 정보
     * @return 최종 찜 상태(wished: true/false)
     */
    @PostMapping("/{performanceId}/wish")
    public CommonResponse<Map<String, Object>> toggleWish(
        @PathVariable Long performanceId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return CommonResponse.ok(Map.of("wished", false), "비로그인: 찜 불가");
        }

        Long userId = userDetails.getUser().getUserId();

        log.info("[UserPerformanceRelation] controller toggleWish - userId={}, performanceId={}",
            userId, performanceId
        );

        boolean wished = userPerformanceRelationService.toggleWish(userId, performanceId);

        return CommonResponse.ok(Map.of("wished", wished), "찜 토글 성공");
    }

    /**
     * 내가 찜한 공연 목록 조회
     *
     * 로그인한 사용자의 WISHED 상태 공연만 조회
     * keyword가 존재하면 공연 제목 기준으로 검색
     *
     * @param userDetails 로그인 사용자 정보
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 당 개수
     * @param keyword 공연 제목 검색어 (선택)
     * @return 찜 공연 목록 페이지
     */
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

    @GetMapping("/{performanceId}/reported")
    public CommonResponse<Map<String, Object>> isReported(
        @PathVariable Long performanceId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            log.info("[UserPerformanceRelation] controller isReported - anonymous, performanceId={}", performanceId);
            return CommonResponse.ok(Map.of("reported", false), "비로그인: 신고 여부 false");
        }

        Long userId = userDetails.getUser().getUserId();
        log.info("[UserPerformanceRelation] controller isReported - anonymous, performanceId={}", performanceId);

        boolean reported = userPerformanceRelationService.isReported(userId, performanceId);

        return CommonResponse.ok(Map.of("reported", reported), "신고 여부 조회 성공");
    }
}
