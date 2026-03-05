package com.encore.encore.domain.performance.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PerformanceReviewReqDto {
    private Integer rating;
    private String content;
    /** Encore pick: 이 공연에서 가장 기억에 남는 곡/장면 한 줄 (선택) */
    private String encorePick;
}
