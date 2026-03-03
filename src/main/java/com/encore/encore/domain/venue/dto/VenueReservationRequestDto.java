package com.encore.encore.domain.venue.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 공연자의 대관 신청 요청 DTO.
 * venueId, startAt, endAt 은 필수값이다.
 */
@Getter
@NoArgsConstructor
public class VenueReservationRequestDto {

    /** 대관 신청할 공연장 ID */
    @NotNull(message = "공연장 ID는 필수입니다.")
    private Long venueId;

    /** 대관 시작 일시 (현재 시각 이후여야 함) */
    @NotNull(message = "시작 일시는 필수입니다.")
    @Future(message = "시작 일시는 현재 시각 이후여야 합니다.")
    private LocalDateTime startAt;

    /** 대관 종료 일시 */
    @NotNull(message = "종료 일시는 필수입니다.")
    private LocalDateTime endAt;

    /** 공연자 신청 메시지 (선택, 최대 1000자) */
    @Size(max = 1000, message = "메시지는 1000자를 초과할 수 없습니다.")
    private String message;
}
