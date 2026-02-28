package com.encore.encore.domain.venue.dto;

import com.encore.encore.domain.venue.entity.Seat;
import com.encore.encore.domain.venue.entity.Venue;
import lombok.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * [설명] 공연장 신규 등록을 위한 요청 DTO (1~3단계 통합)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueCreateRequestDto {

    /* --- [1단계] 공연장 기본 정보 --- */
    private String venueName;     // 공연장 명칭
    private String address;       // 상세 주소
    private String contact;       // 대표 연락처
    private String description;   // 공연장 상세 설명
    private String venueType;     // 시설 유형 (예: 소극장, 다목적 홀 등)
    private String venueImage;    // 대표 이미지 경로

    /* --- [2단계] 좌석 배치 데이터 --- */
    private List<SeatCreateRequest> seats; // 등록 대상 좌석 리스트
    private Integer totalSeats;           // 총 좌석 수

    /* --- [3단계] 운영 시간 --- */
    private String openTime;      // 운영 시작 시간 (HH:mm)
    private String closeTime;     // 운영 종료 시간 (HH:mm)
    private Integer bookingUnit;  // 예약 시간 단위 (예: 30, 60분)
    private Integer rentalFee;    // 시간당 대관료
    private List<String> facilities; // 편의 시설 리스트 (배열 형태)
    private List<String> regularClosingDays;    // 예: ["MONDAY", "SUNDAY"]
    private List<String> temporaryClosingDates; // 예: ["2026-03-01", "2026-05-05"]


    //리스트를 콤마 문자열로 변환
    public String getFacilitiesAsString() {
        return convertListToString(this.facilities);
    }

    public String getRegularClosingDaysAsString() {
        return convertListToString(this.regularClosingDays);
    }

    public String getTemporaryClosingDatesAsString() {
        return convertListToString(this.temporaryClosingDates);
    }

    private String convertListToString(List<String> list) {
        return (list != null && !list.isEmpty()) ? String.join(",", list) : "";
    }

    /**
     * [설명] 입력받은 좌석 DTO 리스트를 Seat 엔티티 리스트로 변환합니다.
     * @param venue 연관관계를 맺을 공연장 엔티티
     * @return 변환된 Seat 엔티티 리스트
     */
    public List<Seat> toSeatEntities(Venue venue) {
        if (this.seats == null || this.seats.isEmpty()) {
            return Collections.emptyList();
        }

        return this.seats.stream()
            .map(s -> Seat.builder()
                .venue(venue) // 연관관계 매핑
                .seatFloor(s.getFloor())
                .xPos(s.getX())
                .yPos(s.getY())
                .seatNumber(s.getNumber())
                .seatType(s.getType())
                .build())
            .collect(Collectors.toList());
    }

    /**
     * [설명] 공연장 내부 개별 좌석 등록 정보
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SeatCreateRequest {
        private Integer floor;    // 층 정보
        private Integer x;        // 캔버스 X 좌표
        private Integer y;        // 캔버스 Y 좌표
        private String number;    // 좌석 번호 (예: "A-1")
        private String type;      // 좌석 등급/유형 (예: "vip", "r")
    }
}
