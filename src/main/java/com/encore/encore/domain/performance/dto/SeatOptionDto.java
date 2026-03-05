package com.encore.encore.domain.performance.dto;

import com.encore.encore.domain.venue.entity.Seat;
import lombok.Getter;

/**
 * [설명] 좌석 리뷰 작성 시 좌석 선택용 DTO (드롭다운 등).
 *
 * @param seatId     좌석 ID
 * @param seatNumber 좌석 번호
 * @param seatType   등급
 * @param seatFloor  층
 */
@Getter
public class SeatOptionDto {

    private final Long seatId;
    private final String seatNumber;
    private final String seatType;
    private final Integer seatFloor;

    public SeatOptionDto(Seat seat) {
        this.seatId = seat.getSeatId();
        this.seatNumber = seat.getSeatNumber() != null ? seat.getSeatNumber() : "";
        this.seatType = seat.getSeatType() != null ? seat.getSeatType() : "";
        this.seatFloor = seat.getSeatFloor();
    }
}
