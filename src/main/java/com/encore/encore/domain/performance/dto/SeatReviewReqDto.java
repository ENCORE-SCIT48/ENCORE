package com.encore.encore.domain.performance.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * [설명] 좌석 리뷰 작성/수정 요청 DTO.
 * 관람객(ROLE_USER)만 사용한다.
 */
@Getter
@Setter
@NoArgsConstructor
public class SeatReviewReqDto {

    /** 대상 좌석 ID (공연 장소의 좌석) */
    private Long seatId;
    /** 별점 (1~5) */
    private Integer rating;
    /** 리뷰 내용 (5자 이상) */
    private String content;
}
