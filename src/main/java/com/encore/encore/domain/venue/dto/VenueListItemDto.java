package com.encore.encore.domain.venue.dto;

import com.encore.encore.domain.venue.entity.Venue;
import lombok.Getter;

@Getter
public class VenueListItemDto {
    private final Long venueId;
    private final String venueName;
    private final String address;
    private final String venueType;
    private final Integer totalSeats;

    public VenueListItemDto(Venue venue) {
        this.venueId = venue.getVenueId();
        this.venueName = venue.getVenueName();
        this.address = venue.getAddress();
        this.venueType = venue.getVenueType();
        this.totalSeats = venue.getTotalSeats();
    }
}
