package com.encore.encore.domain.venue.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/venues")
public class VenuePageController {

    @GetMapping
    public String listPage() {
        log.info("[VenuePage] list page requested");
        return "venue/list";
    }

    // 등록 모드
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("mode", "CREATE");
        return "venue/venueForm";
    }

    // 수정 및 삭제 모드
    @GetMapping("/{venueId}/edit")
    public String editForm(@PathVariable Long venueId, Model model) {
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
    public String reservationFormPage(@PathVariable Long venueId, Model model) {
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

    // [공연자] 공연장 탐색 목록 — 카드 클릭 시 /{venueId}/reservation 으로 이동
    @GetMapping("/performer")
    public String performerVenueListPage() {
        log.info("[VenuePage] performer venue list page requested");
        return "venue/performerVenueList";
    }
}
