package com.encore.encore.domain.performance.dto;

import com.encore.encore.domain.venue.entity.Seat;
import lombok.Getter;

/**
 * 좌석 리뷰 작성·좌석 배치도 표시용 DTO.
 * <p>
 * xPos, yPos가 있으면 공연장 좌석 수정 시와 동일한 비율(0~1000 → 0~1)로 캔버스에 그려
 * 좌석 선택·호버 툴팁에 사용한다. 없으면 드롭다운만 사용.
 * </p>
 *
 * @param seatId     좌석 ID
 * @param seatNumber 좌석 번호 (예: A-1)
 * @param seatType   등급 (vip, r, s, a)
 * @param seatFloor  층
 * @param xPos       캔버스 x 비율용 정수 0~1000 (null이면 위치 미배치)
 * @param yPos       캔버스 y 비율용 정수 0~1000 (null이면 위치 미배치)
 */
@Getter
public class SeatOptionDto {

    private final Long seatId;
    private final String seatNumber;
    private final String seatType;
    private final Integer seatFloor;
    private final Integer xPos;
    private final Integer yPos;

    public SeatOptionDto(Seat seat) {
        this.seatId = seat.getSeatId();
        this.seatNumber = seat.getSeatNumber() != null ? seat.getSeatNumber() : "";
        this.seatType = seat.getSeatType() != null ? seat.getSeatType() : "";
        this.seatFloor = seat.getSeatFloor();
        this.xPos = seat.getXPos();
        this.yPos = seat.getYPos();
    }

    /** 캔버스 그리기용 x 비율 (0~1). 위치 없으면 0 */
    public double getXRatio() {
        return xPos != null ? xPos / 1000.0 : 0.0;
    }

    /** 캔버스 그리기용 y 비율 (0~1). 위치 없으면 0 */
    public double getYRatio() {
        return yPos != null ? yPos / 1000.0 : 0.0;
    }

    /** 위치가 있으면 캔버스 배치 가능 */
    public boolean hasPosition() {
        return xPos != null && yPos != null;
    }
}
