package com.encore.encore.domain.venue.dto;

import com.encore.encore.domain.venue.entity.Seat;
import com.encore.encore.domain.venue.entity.Venue;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 공연장 등록/수정 폼 페이지용 응답 DTO
 * GET /api/venues/{venueId}/form 에서 반환
 * JS prefill() 구조에 맞게 필드명 통일
 */
@Getter
public class VenueFormResponseDto {

    private final Long venueId;
    private final String venueName;
    private final String address;
    private final String contact;
    private final String description;
    private final String venueType;
    private final String imageUrl;       // JS: v.imageUrl
    private final Integer totalSeats;
    private final String openTime;
    private final String closeTime;
    private final Integer bookingUnit;
    private final Integer rentalFee;

    private final List<String> facilities;
    private final List<String> regularClosingDays;
    private final List<String> temporaryClosingDates;

    // flat 좌석 리스트 — JS prefill()에서 floor 기준으로 재그룹핑
    private final List<SeatInfo> seats;

    public VenueFormResponseDto(Venue venue, List<Seat> seats) {
        this.venueId               = venue.getVenueId();
        this.venueName             = venue.getVenueName();
        this.address               = venue.getAddress();
        this.contact               = venue.getContact();
        this.description           = venue.getDescription();
        this.venueType             = venue.getVenueType();
        this.imageUrl              = venue.getVenueImage();
        this.totalSeats            = venue.getTotalSeats();
        this.openTime              = venue.getOpenTime();
        this.closeTime             = venue.getCloseTime();
        this.bookingUnit           = venue.getBookingUnit();
        this.rentalFee             = venue.getRentalFee();
        this.facilities            = splitToList(venue.getFacilities());
        this.regularClosingDays    = splitToList(venue.getRegularClosing());
        this.temporaryClosingDates = splitToList(venue.getTemporaryClosing());
        this.seats = seats == null ? Collections.emptyList()
            : seats.stream().map(SeatInfo::new).toList();
    }

    private List<String> splitToList(String str) {
        if (str == null || str.isBlank()) return Collections.emptyList();
        return Arrays.stream(str.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
    }

    @Getter
    public static class SeatInfo {
        private final Long id;
        private final int floor;
        private final double xRatio;
        private final double yRatio;
        private final String label;   // seatNumber
        private final String grade;   // seatType

        public SeatInfo(Seat seat) {
            this.id     = seat.getSeatId();
            this.floor  = seat.getSeatFloor() != null ? seat.getSeatFloor() : 1;
            this.xRatio = seat.getXPos() != null ? seat.getXPos() / 1000.0 : 0.0;
            this.yRatio = seat.getYPos() != null ? seat.getYPos() / 1000.0 : 0.0;
            this.label  = seat.getSeatNumber();
            this.grade  = seat.getSeatType();
        }
    }
}
