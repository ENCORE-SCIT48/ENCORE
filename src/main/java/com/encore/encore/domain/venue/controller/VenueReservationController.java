package com.encore.encore.domain.venue.controller;

import com.encore.encore.domain.venue.dto.ReservationRejectRequestDto;
import com.encore.encore.domain.venue.dto.VenueReservationRequestDto;
import com.encore.encore.domain.venue.dto.VenueReservationResponseDto;
import com.encore.encore.domain.venue.service.VenueReservationService;
import com.encore.encore.global.common.CommonResponse;
import com.encore.encore.global.config.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 대관 예약 REST 컨트롤러.
 * 공연자의 신청/조회, 호스트의 목록 조회 및 승낙/거절 API를 제공한다.
 *
 * <pre>
 * POST   /api/reservations                      - 대관 신청 (공연자)
 * GET    /api/reservations/my                   - 내 대관 요청 목록 (공연자)
 * GET    /api/venues/{venueId}/reservations     - 공연장별 대관 요청 목록 (호스트)
 * PATCH  /api/reservations/{id}/approve         - 대관 승낙 (호스트)
 * PATCH  /api/reservations/{id}/reject          - 대관 거절 (호스트)
 * </pre>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class VenueReservationController {

    private final VenueReservationService reservationService;

    /**
     * [공연자] 대관을 신청한다.
     *
     * @param userDetails 로그인 사용자 정보 (activeProfileId = performerId)
     * @param dto         대관 신청 요청 DTO
     * @return 201 Created + 생성된 예약 정보
     */
    @PostMapping("/api/reservations")
    public CommonResponse<VenueReservationResponseDto> createReservation(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestBody @Valid VenueReservationRequestDto dto
    ) {
        log.info("[POST /api/reservations] activeProfileId={}", userDetails.getActiveProfileId());
        VenueReservationResponseDto response = reservationService.createReservation(userDetails, dto);
        return CommonResponse.ok(response, "대관 신청이 완료되었습니다.");
    }

    /**
     * [공연자] 내 대관 요청 목록을 조회한다.
     *
     * @param userDetails 로그인 사용자 정보 (activeProfileId = performerId)
     * @return 200 OK + 예약 목록
     */
    @GetMapping("/api/reservations/my")
    public CommonResponse<List<VenueReservationResponseDto>> getMyReservations(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("[GET /api/reservations/my] activeProfileId={}", userDetails.getActiveProfileId());
        List<VenueReservationResponseDto> response = reservationService.getMyReservations(userDetails);
        return CommonResponse.ok(response, "내 대관 요청 목록 조회 성공");
    }

    /**
     * [호스트] 공연장별 대관 요청 목록을 조회한다.
     *
     * @param userDetails 로그인 사용자 정보 (activeProfileId = hostId)
     * @param venueId     조회할 공연장 ID
     * @return 200 OK + 예약 목록
     */
    @GetMapping("/api/venues/{venueId}/reservations")
    public CommonResponse<List<VenueReservationResponseDto>> getReservationsByVenue(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long venueId
    ) {
        log.info("[GET /api/venues/{}/reservations] activeProfileId={}", venueId, userDetails.getActiveProfileId());
        List<VenueReservationResponseDto> response = reservationService.getReservationsByVenue(userDetails, venueId);
        return CommonResponse.ok(response, "공연장 대관 요청 목록 조회 성공");
    }

    /**
     * [호스트] 대관 요청을 승낙한다.
     *
     * @param userDetails   로그인 사용자 정보 (activeProfileId = hostId)
     * @param reservationId 승낙할 예약 ID
     * @return 200 OK + 승낙된 예약 정보
     */
    @PatchMapping("/api/reservations/{id}/approve")
    public CommonResponse<VenueReservationResponseDto> approveReservation(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable("id") Long reservationId
    ) {
        log.info("[PATCH /api/reservations/{}/approve] activeProfileId={}", reservationId, userDetails.getActiveProfileId());
        VenueReservationResponseDto response = reservationService.approveReservation(userDetails, reservationId);
        return CommonResponse.ok(response, "대관 신청을 승낙했습니다.");
    }

    /**
     * [호스트] 대관 요청을 거절한다.
     *
     * @param userDetails   로그인 사용자 정보 (activeProfileId = hostId)
     * @param reservationId 거절할 예약 ID
     * @param dto           거절 사유 DTO
     * @return 200 OK + 거절된 예약 정보
     */
    @PatchMapping("/api/reservations/{id}/reject")
    public CommonResponse<VenueReservationResponseDto> rejectReservation(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable("id") Long reservationId,
        @RequestBody @Valid ReservationRejectRequestDto dto
    ) {
        log.info("[PATCH /api/reservations/{}/reject] activeProfileId={}", reservationId, userDetails.getActiveProfileId());
        VenueReservationResponseDto response = reservationService.rejectReservation(userDetails, reservationId, dto);
        return CommonResponse.ok(response, "대관 신청을 거절했습니다.");
    }
}
