package com.encore.encore.domain.performance.dto;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class PerformanceReviewItemDto {

    private final Long reviewId;
    private final Long userId;
    private final String nickname;
    private final Integer rating;
    private final String content;
    private final LocalDateTime createdAt;

    public PerformanceReviewItemDto(
        Long reviewId,
        Long userId,
        String nickname,
        Integer rating,
        String content,
        LocalDateTime createdAt
    ) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.nickname = (nickname == null || nickname.isBlank()) ? "-" : nickname;
        this.rating = rating;
        this.content = content;
        this.createdAt = createdAt;
    }
}
