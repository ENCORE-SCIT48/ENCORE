package com.encore.encore.domain.venue.dto;

import com.encore.encore.domain.venue.entity.Venue;
import lombok.Getter;

/**
 * GET /api/venues/my 전용 응답 DTO.
 * 호스트 내 공연장 관리 페이지(myVenues)에서 카드 렌더링에 필요한 필드를 포함한다.
 */
@Getter
public class VenueMyListItemDto {
    private final Long venueId;
    private final String venueName;
    private final Long hostId;
    private final String address;
    private final String venueType;
    private final Integer totalSeats;
    private final String openTime;
    private final String closeTime;
    private final Integer rentalFee;
    private final Integer bookingUnit;

    public VenueMyListItemDto(Venue venue) {
        this.venueId     = venue.getVenueId();
        this.venueName   = venue.getVenueName();
        this.hostId      = venue.getHost() != null ? venue.getHost().getHostId() : null;
        this.address     = venue.getAddress();
        this.venueType   = venue.getVenueType();
        this.totalSeats  = venue.getTotalSeats();
        this.openTime    = venue.getOpenTime();
        this.closeTime   = venue.getCloseTime();
        this.rentalFee   = venue.getRentalFee();
        this.bookingUnit = venue.getBookingUnit();
    }
}
