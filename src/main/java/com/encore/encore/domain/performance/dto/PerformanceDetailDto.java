package com.encore.encore.domain.performance.dto;

import com.encore.encore.domain.performance.entity.Performance;
import com.encore.encore.domain.venue.entity.Venue;
import lombok.Getter;

@Getter
public class PerformanceDetailDto {

    private final Long performanceId;
    private final String title;
    private final String description;
    private final String status;
    private final Integer capacity;

    private final String venueName;
    private final String address;

    public PerformanceDetailDto(Performance performance) {
        this.performanceId = performance.getPerformanceId();
        this.title = performance.getTitle();
        this.description = performance.getDescription();
        this.status = performance.getStatus();
        this.capacity = performance.getCapacity();

        Venue venue = performance.getVenue();
        this.venueName = venue != null ? venue.getVenueName() : null;
        this.address = venue != null ? venue.getAddress() : null;
    }
}
