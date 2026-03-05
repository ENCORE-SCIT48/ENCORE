package com.encore.encore.domain.performance.dto;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * [설명] 좌석 리뷰 목록/상세 응답용 DTO.
 *
 * @param reviewId   리뷰 ID
 * @param userId     작성자 회원 ID
 * @param nickname   작성자 닉네임
 * @param rating     별점
 * @param content    리뷰 내용
 * @param seatId     좌석 ID
 * @param seatNumber 좌석 번호 (예: A-1)
 * @param seatType   좌석 등급 (vip, r, s, a 등)
 * @param seatFloor  층
 * @param createdAt  작성 일시
 */
@Getter
public class SeatReviewItemDto {

    private final Long reviewId;
    private final Long userId;
    private final String nickname;
    private final Integer rating;
    private final String content;
    private final Long seatId;
    private final String seatNumber;
    private final String seatType;
    private final Integer seatFloor;
    private final LocalDateTime createdAt;

    public SeatReviewItemDto(
        Long reviewId,
        Long userId,
        String nickname,
        Integer rating,
        String content,
        Long seatId,
        String seatNumber,
        String seatType,
        Integer seatFloor,
        LocalDateTime createdAt
    ) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.nickname = (nickname == null || nickname.isBlank()) ? "-" : nickname;
        this.rating = rating;
        this.content = content;
        this.seatId = seatId;
        this.seatNumber = (seatNumber == null || seatNumber.isBlank()) ? "-" : seatNumber;
        this.seatType = (seatType == null || seatType.isBlank()) ? "-" : seatType;
        this.seatFloor = seatFloor;
        this.createdAt = createdAt;
    }
}
