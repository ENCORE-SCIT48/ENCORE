package com.encore.encore.domain.performance.controller;

import com.encore.encore.domain.performance.dto.PerformanceDetailDto;
import com.encore.encore.domain.performance.dto.PerformanceListItemDto;
import com.encore.encore.domain.performance.dto.PerformanceReviewItemDto;
import com.encore.encore.domain.performance.dto.PerformanceReviewReqDto;
import com.encore.encore.domain.performance.service.PerformanceService;
import com.encore.encore.global.common.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/performances")
public class PerformanceController {

    private final PerformanceService performanceService;

    /**
     * 공연 목록을 조회 (검색/카테고리/페이징 지원)
     * @param keyword 검색어(공연 제목 기준) - null/빈값 가능
     * @param category 카테고리 - null/빈값 가능
     * @param page 페이지 번호(0부터 시작)
     * @param size 페이지 당 개수
     * @return 공연 목록 페이지
     */
    @GetMapping
    public CommonResponse<Page<PerformanceListItemDto>> getPerformances(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String category,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "9") int size
    ) {
        log.info("[Performance] controller list - keyword={}, category={}, page={}, size={}", keyword, category, page, size);

        return CommonResponse.ok(
            performanceService.getPerformances(keyword, category, PageRequest.of(page, size)),
            "공연 목록 조회 성공"
        );
    }

    /**
     * 공연 상세 정보를 조회
     * @param performanceId 공연 ID
     * @return 공연 상세 DTO
     */
    @GetMapping("/{performanceId}")
    public CommonResponse<PerformanceDetailDto> getPerformance(@PathVariable Long performanceId) {
        log.info("[Performance] controller detail - performanceId={}", performanceId);

        return CommonResponse.ok(
            performanceService.getPerformance(performanceId),
            "공연 상세 조회 성공"
        );
    }

    /**
     * 핫한 공연 Top10을 조회 (캐러셀용)
     * @return 핫한 공연 리스트
     */
    @GetMapping("/hot")
    public CommonResponse<List<PerformanceListItemDto>> getHotPerformances() {
        log.info("[Performance] controller hot list");

        return CommonResponse.ok(
            performanceService.getHotPerformances(),
            "핫한 공연 조회 성공"
        );
    }

    @GetMapping("/{performanceId}/reviews")
    public CommonResponse<Page<PerformanceReviewItemDto>> getPerformanceReviews(
        @PathVariable Long performanceId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "latest") String sort
    ) {
        log.info("[Performance] controller reviews - performanceId={}, page={}, size={}, sort={}",
            performanceId, page, size, sort);

        return CommonResponse.ok(
            performanceService.getPerformanceReviews(performanceId, PageRequest.of(page, size), sort),
            "공연 리뷰 조회 성공"
        );
    }

    @GetMapping("/{performanceId}/reviews/summary")
    public CommonResponse<Map<String, Object>> getPerformanceReviewSummary(
        @PathVariable Long performanceId
    ) {
        log.info("[Performance] controller reviews summary - performanceId={}", performanceId);

        return CommonResponse.ok(
            performanceService.getPerformanceReviewSummary(performanceId),
            "공연 리뷰 요약 조회 성공"
        );
    }

    @PostMapping("/{performanceId}/reviews")
    public CommonResponse<Map<String, Object>> createPerformanceReview(
        @PathVariable Long performanceId,
        @RequestBody PerformanceReviewReqDto req,
        @AuthenticationPrincipal com.encore.encore.global.config.CustomUserDetails userDetails
    ) {
        if (userDetails == null || userDetails.getUser() == null) {
            throw new com.encore.encore.global.error.ApiException(
                com.encore.encore.global.error.ErrorCode.UNAUTHORIZED,
                "로그인이 필요합니다."
            );
        }

        Long reviewId = performanceService.createPerformanceReview(
            performanceId,
            userDetails.getUser().getUserId(),
            req.getRating(),
            req.getContent()
        );

        return CommonResponse.ok(Map.of("reviewId", reviewId), "리뷰 작성 성공");
    }

    @GetMapping("/{performanceId}/reviews/{reviewId}")
    public CommonResponse<Map<String, Object>> getReviewForEdit(
        @PathVariable Long performanceId,
        @PathVariable Long reviewId,
        @AuthenticationPrincipal com.encore.encore.global.config.CustomUserDetails userDetails
    ) {
        if (userDetails == null || userDetails.getUser() == null) {
            throw new com.encore.encore.global.error.ApiException(
                com.encore.encore.global.error.ErrorCode.UNAUTHORIZED,
                "로그인이 필요합니다."
            );
        }

        return CommonResponse.ok(
            performanceService.getPerformanceReviewForEdit(
                performanceId,
                reviewId,
                userDetails.getUser().getUserId()
            ),
            "리뷰 조회 성공"
        );
    }

    @PatchMapping("/{performanceId}/reviews/{reviewId}")
    public CommonResponse<Map<String, Object>> updatePerformanceReview(
        @PathVariable Long performanceId,
        @PathVariable Long reviewId,
        @RequestBody PerformanceReviewReqDto req,
        @AuthenticationPrincipal com.encore.encore.global.config.CustomUserDetails userDetails
    ) {
        if (userDetails == null || userDetails.getUser() == null) {
            throw new com.encore.encore.global.error.ApiException(
                com.encore.encore.global.error.ErrorCode.UNAUTHORIZED,
                "로그인이 필요합니다."
            );
        }

        performanceService.updatePerformanceReview(
            performanceId,
            reviewId,
            userDetails.getUser().getUserId(),
            req.getRating(),
            req.getContent()
        );

        return CommonResponse.ok(Map.of("reviewId", reviewId), "리뷰 수정 성공");
    }

    @DeleteMapping("/{performanceId}/reviews/{reviewId}")
    public CommonResponse<Map<String, Object>> deletePerformanceReview(
        @PathVariable Long performanceId,
        @PathVariable Long reviewId,
        @AuthenticationPrincipal com.encore.encore.global.config.CustomUserDetails userDetails
    ) {
        if (userDetails == null || userDetails.getUser() == null) {
            throw new com.encore.encore.global.error.ApiException(
                com.encore.encore.global.error.ErrorCode.UNAUTHORIZED,
                "로그인이 필요합니다."
            );
        }

        performanceService.deletePerformanceReview(
            performanceId,
            reviewId,
            userDetails.getUser().getUserId()
        );

        return CommonResponse.ok(Map.of("reviewId", reviewId), "리뷰 삭제 성공");
    }
}
