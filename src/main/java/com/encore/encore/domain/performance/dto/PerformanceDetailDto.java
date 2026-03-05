package com.encore.encore.domain.performance.dto;

import com.encore.encore.domain.performance.entity.Performance;
import com.encore.encore.domain.performance.entity.PerformanceCategory;
import com.encore.encore.domain.performance.entity.PerformanceStatus;
import com.encore.encore.domain.venue.entity.Venue;
import com.encore.encore.domain.member.entity.PerformerProfile;
import lombok.Getter;

@Getter
public class PerformanceDetailDto {

    private final Long performanceId;
    private final String title;
    private final String description;
    /** 공연 대표 이미지(포스터) URL */
    private final String performanceImageUrl;
    /** 장르 카테고리 */
    private final PerformanceCategory category;
    /** 진행 상태 */
    private final PerformanceStatus status;
    private final Integer capacity;

    private final String venueName;
    private final String address;

    /** 공연장 상세 화면으로 이동하기 위한 ID (있을 때만 세팅) */
    private final Long venueId;

    /** 이 공연을 만든 공연자(퍼포머) 정보 - 있을 때만 세팅 */
    private final Long performerId;
    private final String performerStageName;

    public PerformanceDetailDto(Performance performance) {
        this.performanceId = performance.getPerformanceId();
        this.title = performance.getTitle();
        this.description = performance.getDescription();
        this.performanceImageUrl = performance.getPerformanceImageUrl();
        this.category = performance.getCategory();
        this.status = performance.getStatus();
        this.capacity = performance.getCapacity();

        Venue venue = performance.getVenue();
        this.venueName = venue != null ? venue.getVenueName() : null;
        this.address = venue != null ? venue.getAddress() : null;
        this.venueId = venue != null ? venue.getVenueId() : null;

        PerformerProfile performer = performance.getPerformerCreator();
        this.performerId = performer != null ? performer.getPerformerId() : null;
        this.performerStageName = performer != null ? performer.getStageName() : null;
    }
}
