package com.encore.encore.domain.performance.dto;

import com.encore.encore.domain.performance.entity.Performance;
import lombok.Getter;

@Getter
public class PerformanceListItemDto {

    private final Long performanceId;
    private final String title;
    private final String status;
    private final Integer capacity;

    public PerformanceListItemDto(Performance performance) {
        this.performanceId = performance.getPerformanceId();
        this.title = performance.getTitle();
        this.status = performance.getStatus();
        this.capacity = performance.getCapacity();
    }
}
