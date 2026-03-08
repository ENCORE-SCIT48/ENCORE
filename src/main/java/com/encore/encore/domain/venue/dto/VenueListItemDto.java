package com.encore.encore.domain.venue.dto;

import com.encore.encore.domain.venue.entity.Venue;
import lombok.Getter;

/**
 * GET /api/venues 목록 조회 응답 DTO.
 * 공연자 전용 목록(performerVenueList)에서 카드 렌더링에 필요한 필드를 포함한다.
 */
@Getter
public class VenueListItemDto {
    private final Long venueId;
    private final String venueName;
    private final String address;
    private final String venueType;
    private final Integer totalSeats;
    private final String openTime;
    private final String closeTime;
    private final Integer rentalFee;
    private final Integer bookingUnit;
    private final String imageUrl;

    public VenueListItemDto(Venue venue) {
        this.venueId     = venue.getVenueId();
        this.venueName   = venue.getVenueName();
        this.address     = venue.getAddress();
        this.venueType   = venue.getVenueType();
        this.totalSeats  = venue.getTotalSeats();
        this.openTime    = venue.getOpenTime();
        this.closeTime   = venue.getCloseTime();
        this.rentalFee   = venue.getRentalFee();
        this.bookingUnit = venue.getBookingUnit();
        this.imageUrl   = venue.getVenueImage();
    }
}
