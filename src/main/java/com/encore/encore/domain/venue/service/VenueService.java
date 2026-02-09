package com.encore.encore.domain.venue.service;

import com.encore.encore.domain.venue.dto.VenueDetailDto;
import com.encore.encore.domain.venue.dto.VenueListItemDto;
import com.encore.encore.domain.venue.entity.Venue;
import com.encore.encore.domain.venue.repository.VenueRepository;
import com.encore.encore.global.error.ApiException;
import com.encore.encore.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;

    /**
     * 공연장 목록을 조회. 검색어가 있으면 이름/주소 기준 검색, 없으면 전체 조회.
     * @param keyword 검색어
     * @param pageable 페이징 정보
     * @return 공연장 목록(페이지)
     */
    public Page<VenueListItemDto> getVenues(String keyword, Pageable pageable) {

        // 컨트롤러가 아니라 서비스에서 "검색/전체조회 분기"를 처리해야
        // 다른 API에서도 재사용 가능하고, 컨트롤러가 얇아져 유지보수성이 좋아짐
        Page<Venue> venues = StringUtils.hasText(keyword)
            ? venueRepository.findByVenueNameContainingIgnoreCaseOrAddressContainingIgnoreCase(keyword, keyword, pageable)
            : venueRepository.findAll(pageable);

        log.info("[Venue] list result - keyword={}, totalElements={}", keyword, venues.getTotalElements());
        return venues.map(VenueListItemDto::new);
    }

    /**
     * 공연장 상세 정보를 조회. 대상이 없으면 NOT_FOUND 예외를 발생.
     * @param venueId 공연장 ID
     * @return 공연장 상세 DTO
     */
    public VenueDetailDto getVenue(Long venueId) {
        Venue venue = venueRepository.findById(venueId)
            // detailMessage까지 주면 사용자 메시지/로그 모두 더 명확해짐
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "공연장을 찾을 수 없습니다. venueId=" + venueId));

        log.info("[Venue] detail found - venueId={}", venueId);
        return new VenueDetailDto(venue);
    }
}
