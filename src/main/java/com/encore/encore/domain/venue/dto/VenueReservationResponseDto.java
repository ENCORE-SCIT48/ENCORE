package com.encore.encore.domain.venue.dto;

import com.encore.encore.domain.venue.entity.ReservationStatus;
import com.encore.encore.domain.venue.entity.VenueReservation;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 대관 예약 응답 DTO.
 * Venue / PerformerProfile / HostProfile 의 미머지 필드는 하드코딩 대체값을 사용한다.
 */
@Getter
@Builder
public class VenueReservationResponseDto {

    private Long reservationId;

    // ── Venue 관련 (openTime·closeTime·rentalFee·bookingUnit 미머지 → 하드코딩) ──
    private Long venueId;
    private String venueName;
    private String address;
    /** 하드코딩 대체: "09:00" — openTime 머지 후 venue.getOpenTime() 으로 교체 */
    private String openTime;
    /** 하드코딩 대체: "22:00" — closeTime 머지 후 venue.getCloseTime() 으로 교체 */
    private String closeTime;
    /** 하드코딩 대체: 50000 — rentalFee 머지 후 venue.getRentalFee() 으로 교체 */
    private Integer rentalFee;
    /** 하드코딩 대체: 60 (분) — bookingUnit 머지 후 venue.getBookingUnit() 으로 교체 */
    private Integer bookingUnit;

    // ── 공연자 정보 ──
    private Long performerId;
    private String performerStageName;

    // ── 호스트 정보 ──
    private Long hostId;
    private String hostOrganizationName;

    // ── 예약 정보 ──
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private ReservationStatus status;
    private String message;
    private String rejectReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * VenueReservation 엔티티를 ResponseDto 로 변환한다.
     * Venue 의 미머지 필드(openTime, closeTime, rentalFee, bookingUnit)는
     * 임시 하드코딩 값을 사용하며, 머지 후 실제 getter 로 교체한다.
     *
     * @param r 변환할 VenueReservation 엔티티
     * @return VenueReservationResponseDto
     */
    public static VenueReservationResponseDto from(VenueReservation r) {
        return VenueReservationResponseDto.builder()
            .reservationId(r.getReservationId())
            // Venue
            .venueId(r.getVenue().getVenueId())
            .venueName(r.getVenue().getVenueName())
            .address(r.getVenue().getAddress())
            // TODO: 머지 후 venue.getOpenTime() / getCloseTime() / getRentalFee() / getBookingUnit() 으로 교체
            .openTime("09:00")
            .closeTime("22:00")
            .rentalFee(50000)
            .bookingUnit(60)
            // Performer
            .performerId(r.getPerformer().getPerformerId())
            .performerStageName(r.getPerformer().getStageName())
            // Host
            .hostId(r.getHost().getHostId())
            .hostOrganizationName(r.getHost().getOrganizationName())
            // 예약
            .startAt(r.getStartAt())
            .endAt(r.getEndAt())
            .status(r.getStatus())
            .message(r.getMessage())
            .rejectReason(r.getRejectReason())
            .createdAt(r.getCreatedAt())
            .updatedAt(r.getUpdatedAt())
            .build();
    }
}
