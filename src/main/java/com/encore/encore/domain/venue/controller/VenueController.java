package com.encore.encore.domain.venue.controller;

import com.encore.encore.domain.venue.dto.VenueDetailDto;
import com.encore.encore.domain.venue.dto.VenueListItemDto;
import com.encore.encore.domain.venue.service.VenueService;
import com.encore.encore.global.common.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService venueService;

    /**
     * 공연장 목록을 조회 (검색/페이징 지원)
     * @param keyword 검색어(공연장명 또는 주소) - null/빈값이면 전체 조회
     * @param page 페이지 번호(0부터 시작)
     * @param size 페이지 당 개수
     * @return 공연장 목록 페이지
     */
    @GetMapping
    public CommonResponse<Page<VenueListItemDto>> getVenues(
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        log.info("[Venue] list request - keyword={}, page={}, size={}", keyword, page, size); // [추가] INFO 로그

        return CommonResponse.ok(
            venueService.getVenues(keyword, PageRequest.of(page, size)),
            "공연장 목록 조회 성공"
        );
    }

    /**
     * 공연장 상세 정보를 조회
     * @param venueId 공연장 ID
     * @return 공연장 상세 정보
     */
    @GetMapping("/{venueId}")
    public CommonResponse<VenueDetailDto> getVenue(@PathVariable Long venueId) {
        log.info("[Venue] detail request - venueId={}", venueId); // [추가] INFO 로그

        return CommonResponse.ok(
            venueService.getVenue(venueId),
            "공연장 상세 조회 성공"
        );
    }
}
