package com.encore.encore.domain.venue.dto;

import com.encore.encore.domain.venue.entity.ReservationStatus;
import com.encore.encore.domain.venue.entity.VenueReservation;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class VenueReservationResponseDto {

    private Long reservationId;

    // Venue
    private Long venueId;
    private String venueName;
    private String address;
    private String openTime;
    private String closeTime;
    private Integer rentalFee;
    private Integer bookingUnit;

    // 공연자
    private Long performerId;
    private String performerStageName;

    // 호스트
    private Long hostId;
    private String hostOrganizationName;

    // 예약
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private ReservationStatus status;
    private String message;
    private String rejectReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static VenueReservationResponseDto from(VenueReservation r) {
        return VenueReservationResponseDto.builder()
            .reservationId(r.getReservationId())
            .venueId(r.getVenue().getVenueId())
            .venueName(r.getVenue().getVenueName())
            .address(r.getVenue().getAddress())
            .openTime(r.getVenue().getOpenTime())       // 실제 필드 사용
            .closeTime(r.getVenue().getCloseTime())     // 실제 필드 사용
            .rentalFee(r.getVenue().getRentalFee())     // 실제 필드 사용
            .bookingUnit(r.getVenue().getBookingUnit()) // 실제 필드 사용
            .performerId(r.getPerformer().getPerformerId())
            .performerStageName(r.getPerformer().getStageName())
            .hostId(r.getHost().getHostId())
            .hostOrganizationName(r.getHost().getOrganizationName())
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
