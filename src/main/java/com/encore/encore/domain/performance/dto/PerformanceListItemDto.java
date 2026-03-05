package com.encore.encore.domain.performance.dto;

import com.encore.encore.domain.performance.entity.Performance;
import com.encore.encore.domain.performance.entity.PerformanceCategory;
import com.encore.encore.domain.performance.entity.PerformanceStatus;
import lombok.Getter;

@Getter
public class PerformanceListItemDto {

    private final Long performanceId;
    private final String title;
    /**
     * 공연 대표 이미지(포스터) URL
     */
    private final String performanceImageUrl;
    /**
     * 장르 카테고리
     */
    private final PerformanceCategory category;
    /**
     * 진행 상태
     */
    private final PerformanceStatus status;
    private final Integer capacity;

    public PerformanceListItemDto(Performance performance) {
        this.performanceId = performance.getPerformanceId();
        this.title = performance.getTitle();
        this.performanceImageUrl = performance.getPerformanceImageUrl() != null
            ? performance.getPerformanceImageUrl()
            : "/image/no-image.png";
        this.category = performance.getCategory();
        this.status = performance.getStatus();
        this.capacity = performance.getCapacity();
    }
}
