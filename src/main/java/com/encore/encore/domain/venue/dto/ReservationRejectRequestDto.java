package com.encore.encore.domain.venue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 호스트의 대관 거절 요청 DTO.
 * 거절 사유는 필수값이다.
 */
@Getter
@NoArgsConstructor
public class ReservationRejectRequestDto {

    /** 거절 사유 (필수, 최대 500자) */
    @NotBlank(message = "거절 사유는 필수입니다.")
    @Size(max = 500, message = "거절 사유는 500자를 초과할 수 없습니다.")
    private String rejectReason;
}
