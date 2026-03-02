package com.encore.encore.domain.venue.dto;

import com.encore.encore.domain.venue.entity.Venue;
import lombok.Getter;

/**
 * GET /api/venues/my 전용 응답 DTO.
 * 호스트의 공연장 목록 조회 시 사용하며,
 * list.js 내 공연장 식별을 위해 hostId 를 포함한다.
 * 기존 VenueListItemDto 와 분리하여 독립적으로 관리한다.
 */
@Getter
public class VenueMyListItemDto {

    /** 공연장 ID */
    private final Long venueId;

    /** 공연장 명칭 */
    private final String venueName;

    /** 호스트 프로필 ID (list.js 내 공연장 식별용) */
    private final Long hostId;

    public VenueMyListItemDto(Venue venue) {
        this.venueId   = venue.getVenueId();
        this.venueName = venue.getVenueName();
        this.hostId    = venue.getHost() != null ? venue.getHost().getHostId() : null;
    }
}
