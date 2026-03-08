package com.encore.encore.domain.performance.controller;

import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.domain.performance.dto.PerformanceCreateRequestDto;
import com.encore.encore.domain.performance.dto.PerformanceDetailDto;
import com.encore.encore.domain.performance.dto.PerformanceListItemDto;
import com.encore.encore.domain.performance.dto.PerformanceReviewItemDto;
import com.encore.encore.domain.performance.dto.PerformanceReviewReqDto;
import com.encore.encore.domain.performance.dto.SeatOptionDto;
import com.encore.encore.domain.performance.dto.SeatReviewItemDto;
import com.encore.encore.domain.performance.dto.SeatReviewReqDto;
import com.encore.encore.domain.performance.service.PerformanceService;
import com.encore.encore.global.common.CommonResponse;
import com.encore.encore.global.config.CustomUserDetails;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
        @RequestParam(required = false) Long venueId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "9") int size
    ) {
        log.info("[Performance] controller list - keyword={}, category={}, venueId={}, page={}, size={}",
            keyword, category, venueId, page, size);

        return CommonResponse.ok(
            performanceService.getPerformances(keyword, category, venueId, PageRequest.of(page, size)),
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
     * 공연 등록 (공연자 전용). 공연장과 동일하게 multipart로 포스터 이미지 업로드.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResponse<java.util.Map<String, Object>> createPerformance(
        @RequestPart("performanceData") PerformanceCreateRequestDto dto,
        @RequestPart(value = "performanceImage", required = false) MultipartFile imageFile,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null || userDetails.getUser() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        if (userDetails.getActiveMode() != ActiveMode.ROLE_PERFORMER) {
            throw new ApiException(ErrorCode.FORBIDDEN, "공연자 모드에서만 공연을 등록할 수 있습니다.");
        }

        Long id = performanceService.createPerformance(dto, imageFile, userDetails.getUser());
        return CommonResponse.ok(java.util.Map.of("performanceId", id), "공연 등록 성공");
    }

    /**
     * 공연 수정 (공연자 전용, 본인이 생성한 공연만). 공연장과 동일하게 multipart로 포스터 이미지 업로드.
     */
    @PutMapping(value = "/{performanceId}", consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE })
    public CommonResponse<java.util.Map<String, Object>> updatePerformance(
        @PathVariable Long performanceId,
        @RequestPart("performanceData") PerformanceCreateRequestDto dto,
        @RequestPart(value = "performanceImage", required = false) MultipartFile imageFile,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null || userDetails.getUser() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        if (userDetails.getActiveMode() != ActiveMode.ROLE_PERFORMER) {
            throw new ApiException(ErrorCode.FORBIDDEN, "공연자 모드에서만 공연을 수정할 수 있습니다.");
        }

        Long id = performanceService.updatePerformance(performanceId, dto, imageFile, userDetails.getUser());
        return CommonResponse.ok(java.util.Map.of("performanceId", id), "공연 수정 성공");
    }

    /**
     * 공연 삭제 (논리 삭제, 공연자 전용)
     */
    @DeleteMapping("/{performanceId}")
    public CommonResponse<java.util.Map<String, Object>> deletePerformance(
        @PathVariable Long performanceId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null || userDetails.getUser() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        if (userDetails.getActiveMode() != ActiveMode.ROLE_PERFORMER) {
            throw new ApiException(ErrorCode.FORBIDDEN, "공연자 모드에서만 공연을 삭제할 수 있습니다.");
        }

        performanceService.deletePerformance(performanceId, userDetails.getUser());
        return CommonResponse.ok(java.util.Map.of("performanceId", performanceId), "공연 삭제 성공");
    }

    /**
     * 공연 수정/삭제 가능 여부 조회 (공연자 본인인지 확인)
     */
    @GetMapping("/{performanceId}/ownership")
    public CommonResponse<java.util.Map<String, Object>> getOwnership(
        @PathVariable Long performanceId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        boolean editable = false;
        if (userDetails != null && userDetails.getUser() != null
            && userDetails.getActiveMode() == ActiveMode.ROLE_PERFORMER) {
            editable = performanceService.isEditableByUser(performanceId, userDetails.getUser());
        }
        return CommonResponse.ok(java.util.Map.of("editable", editable), "공연 소유 여부 조회 성공");
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
            req.getContent(),
            req.getEncorePick()
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
            req.getContent(),
            req.getEncorePick()
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

    // ─── 좌석 리뷰 (관람객 전용) ─────────────────────────────────────────────────────

    /**
     * 해당 공연이 열리는 공연장의 좌석 목록 (작성 폼 좌석 선택용)
     */
    @GetMapping("/{performanceId}/seats")
    public CommonResponse<List<SeatOptionDto>> getSeats(
        @PathVariable Long performanceId
    ) {
        log.info("[Performance] controller seats - performanceId={}", performanceId);
        return CommonResponse.ok(
            performanceService.getSeatsByPerformanceId(performanceId),
            "좌석 목록 조회 성공"
        );
    }

    /**
     * 좌석 리뷰 목록 페이징 조회
     */
    @GetMapping("/{performanceId}/seat-reviews")
    public CommonResponse<Page<SeatReviewItemDto>> getSeatReviews(
        @PathVariable Long performanceId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        log.info("[Performance] controller seat-reviews - performanceId={}, page={}, size={}", performanceId, page, size);
        return CommonResponse.ok(
            performanceService.getSeatReviews(performanceId, PageRequest.of(page, size)),
            "좌석 리뷰 목록 조회 성공"
        );
    }

    /**
     * 좌석 리뷰를 좌석 배치도 호버 툴팁용으로 일괄 조회 (seatId별 그룹 후 툴팁 표시용)
     */
    @GetMapping("/{performanceId}/seat-reviews/by-seat")
    public CommonResponse<List<SeatReviewItemDto>> getSeatReviewsBySeat(
        @PathVariable Long performanceId
    ) {
        return CommonResponse.ok(
            performanceService.getSeatReviewsForMap(performanceId),
            "좌석별 리뷰 조회 성공"
        );
    }

    /**
     * 좌석 리뷰 평균 별점·개수 요약
     */
    @GetMapping("/{performanceId}/seat-reviews/summary")
    public CommonResponse<Map<String, Object>> getSeatReviewSummary(
        @PathVariable Long performanceId
    ) {
        log.info("[Performance] controller seat-reviews summary - performanceId={}", performanceId);
        return CommonResponse.ok(
            performanceService.getSeatReviewSummary(performanceId),
            "좌석 리뷰 요약 조회 성공"
        );
    }

    /**
     * 좌석 리뷰 작성 (관람객만 가능)
     */
    @PostMapping("/{performanceId}/seat-reviews")
    public CommonResponse<Map<String, Object>> createSeatReview(
        @PathVariable Long performanceId,
        @RequestBody SeatReviewReqDto req,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null || userDetails.getUser() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        if (userDetails.getActiveMode() != ActiveMode.ROLE_USER) {
            throw new ApiException(ErrorCode.FORBIDDEN, "좌석 리뷰는 관람객만 작성할 수 있습니다.");
        }

        Long reviewId = performanceService.createSeatReview(
            performanceId,
            userDetails.getUser().getUserId(),
            req.getSeatId(),
            req.getRating(),
            req.getContent()
        );
        return CommonResponse.ok(Map.of("reviewId", reviewId), "좌석 리뷰 작성 성공");
    }

    /**
     * 좌석 리뷰 수정 폼용 단건 조회 (작성자 본인만)
     */
    @GetMapping("/{performanceId}/seat-reviews/{reviewId}")
    public CommonResponse<Map<String, Object>> getSeatReviewForEdit(
        @PathVariable Long performanceId,
        @PathVariable Long reviewId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null || userDetails.getUser() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return CommonResponse.ok(
            performanceService.getSeatReviewForEdit(performanceId, reviewId, userDetails.getUser().getUserId()),
            "좌석 리뷰 조회 성공"
        );
    }

    /**
     * 좌석 리뷰 수정 (관람객 본인만)
     */
    @PatchMapping("/{performanceId}/seat-reviews/{reviewId}")
    public CommonResponse<Map<String, Object>> updateSeatReview(
        @PathVariable Long performanceId,
        @PathVariable Long reviewId,
        @RequestBody SeatReviewReqDto req,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null || userDetails.getUser() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        if (userDetails.getActiveMode() != ActiveMode.ROLE_USER) {
            throw new ApiException(ErrorCode.FORBIDDEN, "좌석 리뷰는 관람객만 수정할 수 있습니다.");
        }
        performanceService.updateSeatReview(
            performanceId,
            reviewId,
            userDetails.getUser().getUserId(),
            req.getRating(),
            req.getContent()
        );
        return CommonResponse.ok(Map.of("reviewId", reviewId), "좌석 리뷰 수정 성공");
    }

    /**
     * 좌석 리뷰 삭제 (관람객 본인만)
     */
    @DeleteMapping("/{performanceId}/seat-reviews/{reviewId}")
    public CommonResponse<Map<String, Object>> deleteSeatReview(
        @PathVariable Long performanceId,
        @PathVariable Long reviewId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null || userDetails.getUser() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        if (userDetails.getActiveMode() != ActiveMode.ROLE_USER) {
            throw new ApiException(ErrorCode.FORBIDDEN, "좌석 리뷰는 관람객만 삭제할 수 있습니다.");
        }
        performanceService.deleteSeatReview(performanceId, reviewId, userDetails.getUser().getUserId());
        return CommonResponse.ok(Map.of("reviewId", reviewId), "좌석 리뷰 삭제 성공");
    }
}
