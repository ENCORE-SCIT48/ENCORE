package com.encore.encore.domain.performance.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PerformanceReviewReqDto {
    private Integer rating;
    private String content;
}
