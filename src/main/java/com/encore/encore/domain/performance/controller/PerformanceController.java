package com.encore.encore.domain.performance.controller;

import com.encore.encore.domain.performance.dto.PerformanceDetailDto;
import com.encore.encore.domain.performance.dto.PerformanceListItemDto;
import com.encore.encore.domain.performance.service.PerformanceService;
import com.encore.encore.global.common.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/performances")
public class PerformanceController {

    private final PerformanceService performanceService;

    /**
     * 공연 목록을 조회 (검색/카테고리/페이징 지원)
     * @param keyword 검색어(공연 제목 기준) - null/빈값 가능
     * @param category 카테고리 - null/빈값 가능
     * @param page 페이지 번호(0부터 시작)
     * @param size 페이지 당 개수
     * @return 공연 목록 페이지
     */
    @GetMapping
    public CommonResponse<Page<PerformanceListItemDto>> getPerformances(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String category,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "9") int size
    ) {
        log.info("[Performance] controller list - keyword={}, category={}, page={}, size={}", keyword, category, page, size);

        return CommonResponse.ok(
            performanceService.getPerformances(keyword, category, PageRequest.of(page, size)),
            "공연 목록 조회 성공"
        );
    }

    /**
     * 공연 상세 정보를 조회
     * @param performanceId 공연 ID
     * @return 공연 상세 DTO
     */
    @GetMapping("/{performanceId}")
    public CommonResponse<PerformanceDetailDto> getPerformance(@PathVariable Long performanceId) {
        log.info("[Performance] controller detail - performanceId={}", performanceId);

        return CommonResponse.ok(
            performanceService.getPerformance(performanceId),
            "공연 상세 조회 성공"
        );
    }

    /**
     * 핫한 공연 Top10을 조회 (캐러셀용)
     * @return 핫한 공연 리스트
     */
    @GetMapping("/hot")
    public CommonResponse<List<PerformanceListItemDto>> getHotPerformances() {
        log.info("[Performance] controller hot list");

        return CommonResponse.ok(
            performanceService.getHotPerformances(),
            "핫한 공연 조회 성공"
        );
    }
}
