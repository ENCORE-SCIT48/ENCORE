package com.encore.encore.domain.venue.controller;

import com.encore.encore.domain.member.entity.ActiveMode;
import com.encore.encore.global.config.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/venues")
public class VenuePageController {

    /**
     * 공연장 목록 (단일). 프로필에 따라 카드 클릭 동작만 다름.
     * - 유저(ROLE_USER): 공연장 상세(좌석 리뷰) → /venues/{id}
     * - 공연자(ROLE_PERFORMER): 대관 신청 → /venues/{id}/reservation
     */
    @GetMapping
    public String listPage(
        Model model,
        jakarta.servlet.http.HttpServletRequest request,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String activeMode = (userDetails != null && userDetails.getActiveMode() != null)
            ? userDetails.getActiveMode().name()
            : ActiveMode.ROLE_USER.name();
        log.info("[VenuePage] list page requested, activeMode={}", activeMode);

        model.addAttribute("targetUrl", request != null ? request.getRequestURI() : "/venues");
        model.addAttribute("activeMode", activeMode);
        return "venue/list";
    }

    /**
     * 기존 공연자용 URL → 동일 목록으로 리다이렉트 (리스트 하나로 통합)
     */
    @GetMapping("/performer")
    public String performerVenueListRedirect() {
        log.info("[VenuePage] /venues/performer → /venues redirect");
        return "redirect:/venues";
    }

    /**
     * 공연장 상세 페이지 (좌석 리뷰 작성 진입점 포함)
     *
     * @param venueId 공연장 ID
     * @param model   venueId 전달
     * @return venue/detail 템플릿
     */
    @GetMapping("/{venueId}")
    public String detailPage(
        @PathVariable Long venueId,
        Model model,
        jakarta.servlet.http.HttpServletRequest request
    ) {
        log.info("[VenuePage] detail page requested - venueId={}", venueId);
        model.addAttribute("venueId", venueId);
        model.addAttribute("targetUrl", request != null ? request.getRequestURI() : ("/venues/" + venueId));
        return "venue/detail";
    }

    // 등록 모드
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "CREATE");
        return "venue/venueForm";
    }

    // 수정 및 삭제 모드
    @GetMapping("/{venueId}/edit")
    public String editForm(@PathVariable("venueId") Long venueId, Model model) {
        model.addAttribute("venueId", venueId);
        model.addAttribute("mode", "UPDATE");
        return "venue/venueForm";
    }

    // [호스트] 내 공연장 관리 페이지
    @GetMapping("/my")
    public String myVenuesPage() {
        log.info("[VenuePage] my venues page requested");
        return "venue/myVenues";
    }

    // [공연자] 내 대관 신청 목록 페이지
    @GetMapping("/reservations/my")
    public String myReservationsPage() {
        log.info("[VenuePage] my reservations page requested");
        return "venue/myReservations";
    }

    // [공연자] 대관 신청 폼
    @GetMapping("/{venueId}/reservation")
    public String reservationFormPage(@PathVariable("venueId") Long venueId, Model model) {
        log.info("[VenuePage] reservation form requested - venueId={}", venueId);
        model.addAttribute("venueId", venueId);
        return "venue/reservationForm";
    }

    // [호스트] 대관 요청 관리 (탭 방식 — venueId 불필요, venueReservations.js 가 /api/venues/my 로 탭 구성)
    @GetMapping("/reservations/manage")
    public String venueReservationsPage() {
        log.info("[VenuePage] venue reservations manage page requested");
        return "venue/venueReservations";
    }

}
