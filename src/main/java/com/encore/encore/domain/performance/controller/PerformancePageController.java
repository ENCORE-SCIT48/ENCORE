package com.encore.encore.domain.performance.controller;

import com.encore.encore.global.config.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/performances")
public class PerformancePageController {

    /**
     * 공연 목록 페이지 반환
     * - fragments/profile :: profile(targetUrl) 프래그먼트에서 targetUrl 파라미터를 요구하므로
     *   현재 요청 URI를 모델에 담아 내려준다.
     * @return 공연 목록 화면 템플릿 경로
     */
    @GetMapping
    public String listPage(HttpServletRequest request, Model model) { // 파라미터 추가
        log.info("[PerformancePage] list page requested");

        // 현재 페이지(/performances)로 돌아오기 위한 targetUrl 전달
        model.addAttribute("targetUrl", request.getRequestURI());

        return "performance/list";
    }

    /**
     * 공연 상세 페이지 반환 (데이터는 JS에서 /api/performances/{id} 호출로 조회)
     * @param performanceId 공연 ID
     * @param model Thymeleaf 모델(화면에 performanceId, loginUserId 전달)
     * @return 공연 상세 화면 템플릿 경로
     */
    @GetMapping("/{performanceId}")
    public String detailPage(
        @PathVariable Long performanceId,
        Model model,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("[PerformancePage] detail page requested - performanceId={}", performanceId);

        Long loginUserId = (userDetails != null && userDetails.getUser() != null)
            ? userDetails.getUser().getUserId()
            : 0L;

        model.addAttribute("performanceId", performanceId);
        model.addAttribute("loginUserId", loginUserId);
        model.addAttribute("targetUrl", "/performances");

        return "performance/detail";
    }

    /**
     * 공연 리뷰 작성 페이지 반환
     */
    @GetMapping("/{performanceId}/reviews/new")
    public String reviewWritePage(
        @PathVariable Long performanceId,
        Model model,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("[PerformancePage] review write page requested - performanceId={}", performanceId);

        Long loginUserId = (userDetails != null && userDetails.getUser() != null)
            ? userDetails.getUser().getUserId()
            : 0L;

        model.addAttribute("performanceId", performanceId);
        model.addAttribute("loginUserId", loginUserId);

        return "performance/reviewWrite";
    }

    @GetMapping("/{performanceId}/reviews/{reviewId}/edit")
    public String reviewEditPage(
        @PathVariable Long performanceId,
        @PathVariable Long reviewId,
        Model model,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long loginUserId = (userDetails != null && userDetails.getUser() != null)
            ? userDetails.getUser().getUserId()
            : 0L;

        model.addAttribute("performanceId", performanceId);
        model.addAttribute("reviewId", reviewId);
        model.addAttribute("loginUserId", loginUserId);

        // 화면은 reviewWrite 재사용
        return "performance/reviewWrite";
    }

    /**
     * 좌석 리뷰 작성 페이지 (관람객 전용 — API에서 ROLE_USER 검증)
     */
    @GetMapping("/{performanceId}/reviews/seats/new")
    public String seatReviewWritePage(
        @PathVariable Long performanceId,
        Model model,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("[PerformancePage] seat review write page - performanceId={}", performanceId);

        Long loginUserId = (userDetails != null && userDetails.getUser() != null)
            ? userDetails.getUser().getUserId()
            : 0L;

        model.addAttribute("performanceId", performanceId);
        model.addAttribute("loginUserId", loginUserId);
        model.addAttribute("reviewId", 0L);

        return "performance/seatReviewWrite";
    }

    /**
     * 좌석 리뷰 수정 페이지 (관람객 본인만 — API에서 검증)
     */
    @GetMapping("/{performanceId}/reviews/seats/{reviewId}/edit")
    public String seatReviewEditPage(
        @PathVariable Long performanceId,
        @PathVariable Long reviewId,
        Model model,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("[PerformancePage] seat review edit page - performanceId={}, reviewId={}", performanceId, reviewId);

        Long loginUserId = (userDetails != null && userDetails.getUser() != null)
            ? userDetails.getUser().getUserId()
            : 0L;

        model.addAttribute("performanceId", performanceId);
        model.addAttribute("reviewId", reviewId);
        model.addAttribute("loginUserId", loginUserId);

        return "performance/seatReviewWrite";
    }
}
