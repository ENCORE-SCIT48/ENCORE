package com.encore.encore.domain.performance.dto;

import com.encore.encore.domain.community.entity.Review;
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

    public PerformanceReviewItemDto(Review review) {
        this.reviewId = review.getReviewId();
        this.userId = review.getUser().getUserId();
        this.nickname = review.getUser() != null ? review.getUser().getNickname() : "-";
        this.rating = review.getRating();
        this.content = review.getContent();
        this.createdAt = review.getCreatedAt();
    }
}
