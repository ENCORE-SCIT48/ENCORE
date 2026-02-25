package com.encore.encore.domain.venue.dto;

import com.encore.encore.domain.venue.entity.Venue;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class VenueDetailDto {

    private final Long venueId;
    private final String venueName;
    private final String address;
    private final String contact;
    private final String description;
    private final String venueType;
    private final String venueImage;
    private final Integer totalSeats;
    private final String openTime;
    private final String closeTime;
    private final Integer bookingUnit;
    private final Integer rentalFee;

    // 문자열을 리스트로 변환하여 프론트에 전달
    private final List<String> facilities;
    private final List<String> regularClosingDays;    // ["MONDAY", "SUNDAY"]
    private final List<String> temporaryClosingDates; // ["2026-03-01", "2026-05-05"]

    public VenueDetailDto(Venue venue) {
        this.venueId = venue.getVenueId();
        this.venueName = venue.getVenueName();
        this.address = venue.getAddress();
        this.contact = venue.getContact();
        this.description = venue.getDescription();
        this.venueType = venue.getVenueType();
        this.venueImage = venue.getVenueImage();
        this.totalSeats = venue.getTotalSeats();
        this.openTime = venue.getOpenTime();
        this.closeTime = venue.getCloseTime();
        this.bookingUnit = venue.getBookingUnit();
        this.rentalFee = venue.getRentalFee();

        // --- 문자열 -> 리스트 변환 로직 (Null 방어) ---
        this.facilities = splitToList(venue.getFacilities());
        this.regularClosingDays = splitToList(venue.getRegularClosing());
        this.temporaryClosingDates = splitToList(venue.getTemporaryClosing());
    }

    /**
     * 콤마로 구분된 문자열을 리스트로 안전하게 분리합니다.
     */
    private List<String> splitToList(String str) {
        if (str == null || str.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(str.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }
}
